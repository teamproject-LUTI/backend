package com.luti.board.repository;

import com.luti.board.entity.ReviewAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewAttachmentRepository
        extends JpaRepository<ReviewAttachment, Long> {

    /**
     * 특정 후기(reviewNo)에 속한 첨부파일들을 모두 조회
     * -> ReviewAttachment.review.reviewNo 프로퍼티 기준
     */
    List<ReviewAttachment> findAllByReviewReviewNo(Long reviewNo);

    // 아래 형태도 가능 (언더스코어 구분자)
    // List<ReviewAttachment> findAllByReview_ReviewNo(Long reviewNo);
}
