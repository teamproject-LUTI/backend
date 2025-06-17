package com.luti.board.repository;

import com.luti.board.entity.AskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 문의 첨부파일 JPA 레포지토리
 */
@Repository
public interface AskAttachmentRepository extends JpaRepository<AskAttachment, Long> {
    /**
     * 특정 문의글에 딸린 첨부파일 전체 조회
     */
    List<AskAttachment> findByAskAskId(Long askId);
}
