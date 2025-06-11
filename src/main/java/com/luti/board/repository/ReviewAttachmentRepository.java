package com.luti.board.repository;

import com.luti.board.entity.ReviewAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewAttachmentRepository extends JpaRepository<ReviewAttachment, Long> {

    // 특정 후기글의 첨부파일 목록 조회
    List<ReviewAttachment> findByReviewId(Long reviewId);


}
