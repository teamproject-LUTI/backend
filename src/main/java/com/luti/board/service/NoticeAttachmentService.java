package com.luti.board.service;

import com.luti.board.dto.NoticeAttachmentRequestDto;
import com.luti.board.dto.NoticeAttachmentResponseDto;
import com.luti.board.entity.Notice;
import com.luti.board.entity.NoticeAttachment;
import com.luti.board.repository.NoticeAttachmentRepository;
import com.luti.board.repository.NoticeRepository;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeAttachmentService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository attachmentRepository;

    // 기본 업로드 경로 (프로젝트 루트 기준)
//    private static final String UPLOAD_DIR = "uploads/notices";

    // 기본 업로드 경로 (강사님 PC 드라이브 기준)
    @Value("${file.upload.general.dir}")
    private String uploadDir;

    /**
     * 특정 공지사항의 첨부파일 목록을 조회합니다.
     */
    public MultiResponseDto<NoticeAttachmentResponseDto> getAttachments(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        List<NoticeAttachmentResponseDto> dtos = attachmentRepository
                .findAllByNotice(notice)
                .stream()
                .map(NoticeAttachmentResponseDto::of)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(dtos, null);
    }

    /**
     * 공지사항에 첨부파일을 추가합니다. (MultipartFile 처리)
     */
    @Transactional
    public SingleResponseDto<NoticeAttachmentResponseDto> addAttachmentFiles(
            Long noticeId,
            List<MultipartFile> files) {

        // 1) 공지사항 엔티티 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        // 2) MultipartFile 리스트를 순회하며 파일 저장 및 엔티티 생성
        List<NoticeAttachment> savedList = files.stream().map(file -> {
            // 원본 파일명, 확장자, UUID 기반 저장 파일명 생성
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.') + 1);
            }
            String uuid = UUID.randomUUID().toString();
            String storedName = uuid + "." + ext;

            // 업로드 디렉터리 확인 및 생성
//            File dir = new File(UPLOAD_DIR);
//            if (!dir.exists()) dir.mkdirs();
//            Path path = dir.toPath().resolve(storedName);

            // 업로드 디렉터리 확인 및 생성(강사님 PC 드라이브 경로)
            // 공용 DB 업로드 디렉터리 확인 및 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create upload directory: " + uploadDir);
                }
            }
            Path path = dir.toPath().resolve(storedName);

            // 파일 저장
            try {
                byte[] data = file.getBytes();
                Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file " + original, e);
            }

            // 저장된 파일 크기 로깅
            try {
                long savedSize = Files.size(path);
                System.out.printf("📊 Notice attachment - original size=%d, saved size=%d%n", file.getSize(), savedSize);
            } catch (IOException ignored) { }

            // NoticeAttachment 엔티티 빌드 및 양방향 링크 설정
            NoticeAttachment attachment = NoticeAttachment.builder()
                    .fileName(original)
                    .physicalPath(path.toAbsolutePath().toString())
                    .logicalPath("/uploads/notices/" + storedName)
                    .extension(ext)
                    .size(file.getSize())
                    .build();

            // 양방향 연관관계 설정
            notice.addAttachment(attachment);

            // DB에 저장하고 엔티티 반환
            return attachmentRepository.save(attachment);
        }).collect(Collectors.toList());

        // 3) 저장된 첫 번째 파일만 DTO 변환하여 반환
        NoticeAttachmentResponseDto dto = NoticeAttachmentResponseDto.of(savedList.get(0));
        return new SingleResponseDto<>(dto);
    }

    /**
     * 공지사항에 첨부파일을 추가합니다. (메타데이터 방식 - 기존 유지)
     */
    @Transactional
    public SingleResponseDto<NoticeAttachmentResponseDto> addAttachment(
            Long noticeId,
            NoticeAttachmentRequestDto dto) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        // 엔티티 생성 및 양방향 연관관계 설정
        NoticeAttachment attachment = NoticeAttachment.builder()
                .fileName(dto.getFileName())
                .physicalPath(dto.getPhysicalPath())
                .logicalPath(dto.getLogicalPath())
                .extension(dto.getExtension())
                .size(dto.getSize())
                .build();

        notice.addAttachment(attachment);
        NoticeAttachment saved = attachmentRepository.save(attachment);

        return new SingleResponseDto<>(NoticeAttachmentResponseDto.of(saved));
    }

    /**
     * 단일 첨부파일을 조회하여 DTO로 반환
     */
    @Transactional(readOnly = true)
    public NoticeAttachmentResponseDto getAttachmentDto(Long fileNo) {
        NoticeAttachment attachment = attachmentRepository.findById(fileNo)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + fileNo));
        return NoticeAttachmentResponseDto.of(attachment);
    }

    /**
     * 첨부파일을 삭제합니다. (완전 삭제)
     */
    @Transactional
    public void deleteAttachment(Long fileNo) {
        NoticeAttachment attachment = attachmentRepository.findById(fileNo)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + fileNo));

        // 양방향 연관관계 해제 후 삭제
        attachment.unlinkFromNotice();
        attachmentRepository.delete(attachment);
    }
}