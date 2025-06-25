package com.luti.board.service;

import com.luti.board.dto.AskAttachmentRequestDto;
import com.luti.board.dto.ReviewAttachmentRequestDto;
import com.luti.board.dto.ReviewAttachmentResponseDto;
import com.luti.board.entity.Ask;
import com.luti.board.entity.AskAttachment;
import com.luti.board.entity.Review;
import com.luti.board.entity.ReviewAttachment;
import com.luti.board.repository.ReviewAttachmentRepository;
import com.luti.board.repository.ReviewRepository;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewAttachmentService {

    private final ReviewRepository reviewRepository;
    private final ReviewAttachmentRepository attachmentRepository;

    // 기본 업로드 경로 (프로젝트 루트 기준)
//    private static final String UPLOAD_DIR = "uploads";

    // 기본 업로드 경로 (강사님 PC 드라이브 기준)
    @Value("${file.upload.general.dir}")
    private String uploadDir;
    /**
     * 특정 리뷰의 첨부파일 목록 조회
     */
    public MultiResponseDto<ReviewAttachmentResponseDto> getAttachments(Long reviewId) {
        // 리뷰 존재 여부 체크
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));

        List<ReviewAttachmentResponseDto> dtos = attachmentRepository
                .findAllByReviewReviewId(reviewId)
                .stream()
                .map(ReviewAttachmentResponseDto::fromEntity)
                .collect(Collectors.toList());

        // 페이지네이션 없이 전체 반환 (두 번째 파라미터에 페이징 정보 넣을 수 있음)
        return new MultiResponseDto<>(dtos, null);
    }

    /**
     * 리뷰에 첨부파일을 추가 저장 (MultipartFile 처리)
     *
     * @param reviewId 글 ID
     * @param files    업로드된 MultipartFile 리스트
     * @return 저장된 첫 번째 첨부파일의 정보를 담은 SingleResponseDto
     * @throws EntityNotFoundException 리뷰가 존재하지 않으면 발생
     * @throws RuntimeException       파일 저장 중 오류 발생 시 래핑하여 던짐
     */
    @Transactional
    public SingleResponseDto<ReviewAttachmentResponseDto> addAttachmentFiles(
            Long reviewId,
            List<MultipartFile> files) {

        // 1) 리뷰 엔티티 조회 (존재하지 않으면 예외)
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));

        // 2) MultipartFile 리스트를 순회하며 파일 저장 및 엔티티 생성
        List<ReviewAttachment> savedList = files.stream().map(file -> {
            // 원본 파일명, 확장자, UUID 기반 저장 파일명 생성
            String original = file.getOriginalFilename();
            String ext = original.substring(original.lastIndexOf('.') + 1);
            String uuid = UUID.randomUUID().toString();
            String storedName = uuid + "." + ext;

            // 업로드 디렉터리 확인 및 생성(프로젝트 내부 경로)
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

            // file.getBytes()로 파일 바이트 통째로 디스크에 쓰기
            try {
                byte[] data = file.getBytes();
                Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file " + original, e);
            }
            // 저장된 파일 크기 로깅 (원본 vs 저장된 크기 비교)
            try {
                long savedSize = Files.size(path);
                System.out.printf("📊 original size=%d, saved size=%d%n", file.getSize(), savedSize);
            } catch (IOException ignored) { }

            // ReviewAttachment 엔티티 빌드 및 양방향 링크 설정
            ReviewAttachment attach = ReviewAttachment.builder()
                    .fileName(original)
                    .physicalPath(path.toAbsolutePath().toString())
                    .logicalPath("/uploads/" + storedName)
                    .extension(ext)
                    .size(file.getSize())
                    .build();
            attach.linkToReview(review);

            // DB에 저장하고 엔티티 반환
            return attachmentRepository.save(attach);
        }).collect(Collectors.toList());

        // 3) 저장된 첫 번째 파일만 DTO 변환하여 반환
        ReviewAttachmentResponseDto dto =
                ReviewAttachmentResponseDto.fromEntity(savedList.get(0));
        return new SingleResponseDto<>(dto);
    }

    /**
     * 단일 첨부파일을 조회하여 DTO로 반환하는 서비스 메서드
     *
     * @param fileNo - 조회할 첨부파일의 고유 ID
     * @return 파일 정보가 담긴 ReviewAttachmentResponseDto
     * @throws EntityNotFoundException 해당 ID의 첨부파일이 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public ReviewAttachmentResponseDto getAttachmentDto(Long fileNo) {
        // 1) 파일번호(fileNo)로 ReviewAttachment 엔티티 조회
        ReviewAttachment att = attachmentRepository.findById(fileNo)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + fileNo));
        // 2) 엔티티를 DTO로 변환하여 반환
        return ReviewAttachmentResponseDto.fromEntity(att);
    }

    /**
     * 첨부파일 등록 (메타데이터 방식 - 기존 유지)
     */
    @Transactional
    public Long saveAttachment(Long reviewId, ReviewAttachmentRequestDto req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("후기글을 찾을 수 없습니다. reviewId=" + reviewId));

        ReviewAttachment attachment = ReviewAttachment.builder()
                .fileName(req.getFileName())
                .physicalPath(req.getPhysicalPath())
                .logicalPath(req.getLogicalPath())
                .extension(req.getExtension())
                .size(req.getSize())
                .build();
        attachment.linkToReview(review);

        attachmentRepository.save(attachment);
        return attachment.getReviewAttachmentId();
    }

    /**
     * 첨부파일 삭제 (완전 삭제)
     */
    @Transactional
    public void deleteAttachment(Long fileNo) {
        ReviewAttachment attachment = attachmentRepository.findById(fileNo)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + fileNo));

        // 물리 파일 삭제
        try {
            Path filePath = Paths.get(attachment.getPhysicalPath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("🗑️ Physical file deleted: " + filePath.toAbsolutePath());
            } else {
                System.out.println("⚠️ Physical file not found: " + filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to delete physical file: " + attachment.getPhysicalPath());
            // 물리 파일 삭제 실패해도 DB 삭제는 진행
        }

        // 양방향 연관관계 해제 후 DB에서 삭제
        attachment.unlinkFromReview();
        attachmentRepository.delete(attachment);
    }
}
