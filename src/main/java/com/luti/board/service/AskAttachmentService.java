package com.luti.board.service;

import com.luti.board.dto.AskAttachmentRequestDto;
import com.luti.board.dto.AskAttachmentResponseDto;
import com.luti.board.entity.Ask;
import com.luti.board.entity.AskAttachment;
import com.luti.board.repository.AskAttachmentRepository;
import com.luti.board.repository.AskRepository;
import com.luti.dto.SingleResponseDto;
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

/**
 * 문의 첨부파일 기능을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskAttachmentService {

    private final AskAttachmentRepository attachmentRepository;
    private final AskRepository askRepository;

    // 기본 업로드 경로 (프로젝트 루트 기준)
//    private static final String UPLOAD_DIR = "uploads/asks";

    // 기본 업로드 경로 (강사님 PC 드라이브 기준)
    @Value("${file.upload.general.dir}")
    private String uploadDir;

    /**
     * 문의글에 첨부파일을 추가합니다. (MultipartFile 처리)
     */
    @Transactional
    public SingleResponseDto<AskAttachmentResponseDto> addAttachmentFiles(
            Long askId,
            List<MultipartFile> files) {

        // 1) 문의글 엔티티 조회
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다. askId=" + askId));

        // 2) MultipartFile 리스트를 순회하며 파일 저장 및 엔티티 생성
        List<AskAttachment> savedList = files.stream().map(file -> {
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
                System.out.printf("📊 Ask attachment - original size=%d, saved size=%d%n", file.getSize(), savedSize);
            } catch (IOException ignored) { }

            // AskAttachment 엔티티 빌드 및 양방향 링크 설정
            AskAttachment attachment = AskAttachment.builder()
                    .fileName(original)
                    .physicalPath(path.toAbsolutePath().toString())
                    .logicalPath("/uploads/asks/" + storedName)
                    .extension(ext)
                    .size(file.getSize())
                    .build();
            attachment.linkToAsk(ask);

            // DB에 저장하고 엔티티 반환
            return attachmentRepository.save(attachment);
        }).collect(Collectors.toList());

        // 3) 저장된 첫 번째 파일만 DTO 변환하여 반환
        AskAttachmentResponseDto dto = AskAttachmentResponseDto.of(savedList.get(0));
        return new SingleResponseDto<>(dto);
    }

    /**
     * 첨부파일 등록 (메타데이터 방식 - 기존 유지)
     */
    @Transactional
    public Long saveAttachment(Long askId, AskAttachmentRequestDto req) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다. askId=" + askId));

        AskAttachment attachment = AskAttachment.builder()
                .fileName(req.getFileName())
                .physicalPath(req.getPhysicalPath())
                .logicalPath(req.getLogicalPath())
                .extension(req.getExtension())
                .size(req.getSize())
                .build();
        attachment.linkToAsk(ask);

        attachmentRepository.save(attachment);
        return attachment.getAskAttachmentId();
    }

    /**
     * 문의글에 딸린 첨부파일 전체 조회
     */
    public List<AskAttachmentResponseDto> getAttachmentsByAsk(Long askId) {
        return attachmentRepository.findByAskAskId(askId).stream()
                .map(AskAttachmentResponseDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 단일 첨부파일 조회
     */
    public AskAttachmentResponseDto getAttachment(Long id) {
        AskAttachment a = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + id));
        return AskAttachmentResponseDto.of(a);
    }

    /**
     * 첨부파일 삭제
     */
    @Transactional
    public void deleteAttachment(Long id) {
        AskAttachment a = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + id));

        // 양방향 연관관계 해제 후 삭제
        a.unlinkFromAsk();
        attachmentRepository.delete(a);
    }
}