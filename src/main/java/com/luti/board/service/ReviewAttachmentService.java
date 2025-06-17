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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewAttachmentService {

    private final ReviewRepository reviewRepository;
    private final ReviewAttachmentRepository attachmentRepository;

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
     * 리뷰에 첨부파일 추가
     */
    @Transactional
    public SingleResponseDto<ReviewAttachmentResponseDto> addAttachment(
            Long reviewId,
            ReviewAttachmentRequestDto dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));

        // 엔티티 생성 및 양방향 연관관계 설정
        ReviewAttachment attachment = ReviewAttachment.builder()
                .fileName(dto.getFileName())
                .physicalPath(dto.getPhysicalPath())
                .logicalPath(dto.getLogicalPath())
                .extension(dto.getExtension())
                .size(dto.getSize())  // RequestDto의 Long → Entity의 Integer
                .build();
        attachment.linkToReview(review);

        ReviewAttachment saved = attachmentRepository.save(attachment);
        return new SingleResponseDto<>(ReviewAttachmentResponseDto.fromEntity(saved));
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



