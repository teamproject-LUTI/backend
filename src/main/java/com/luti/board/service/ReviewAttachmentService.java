package com.luti.board.service;

import com.luti.board.dto.ReviewAttachmentRequestDto;
import com.luti.board.dto.ReviewAttachmentResponseDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
    private static final String UPLOAD_DIR = "uploads";

    // 기본 업로드 경로 (강사님 PC 드라이브 기준)
//    @Value("${file.upload.general.dir}")
//    private String uploadDir;
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
     * 리뷰에 첨부파일 추가 (MultipartFile 처리)
     */
    @Transactional
    public SingleResponseDto<ReviewAttachmentResponseDto> addAttachmentFiles(
            Long reviewId,
            List<MultipartFile> files) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));

        List<ReviewAttachment> savedList = files.stream().map(file -> {
            String original = file.getOriginalFilename();
            String ext = original.substring(original.lastIndexOf('.') + 1);
            String uuid = UUID.randomUUID().toString();
            String storedName = uuid + "." + ext;
            File dest = new File(UPLOAD_DIR, storedName);    // 프로젝트 내부 경로
            //File dest = new File(uploadDir, storedName);        // 강사님 PC 드라이브 경로
            System.out.println("저장될 파일 경로: " + dest.getAbsolutePath());
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file " + original, e);
            }

            ReviewAttachment attach = ReviewAttachment.builder()
                    .fileName(original)
                    .physicalPath(dest.getAbsolutePath())
                    .logicalPath("/uploads/" + storedName)
                    .extension(ext)
                    .size(file.getSize())
                    .build();
            attach.linkToReview(review);
            return attachmentRepository.save(attach);
        }).collect(Collectors.toList());

        ReviewAttachmentResponseDto dto = ReviewAttachmentResponseDto.fromEntity(savedList.get(0));
        return new SingleResponseDto<>(dto);
    }


    /**
     * 첨부파일 삭제 (완전 삭제)
     */
    @Transactional
    public void deleteAttachment(Long fileNo) {
        ReviewAttachment attachment = attachmentRepository.findById(fileNo)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + fileNo));

        // 양방향 연관관계 해제 후 삭제
        attachment.unlinkFromReview();
        attachmentRepository.delete(attachment);
    }
}
