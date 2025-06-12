package com.luti.board.repository;

import com.luti.board.entity.NoticeAttachment;
import com.luti.board.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachment, Long> {

    /**
     * 특정 공지사항(Notice)에 속한 모든 첨부파일을 조회합니다.
     *
     * @param notice 조회 대상 Notice 엔티티
     * @return 해당 공지사항의 첨부파일 목록
     */
    List<NoticeAttachment> findAllByNotice(Notice notice);
}
