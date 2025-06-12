package com.luti.board.repository;

import com.luti.board.entity.ReviewAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewAttachmentRepository extends JpaRepository<ReviewAttachment, Long> {

    /** 특정 게시물의 첨부파일 목록 조회 */
    List<ReviewAttachment> findByReviewNo(Long reviewNo);


}
