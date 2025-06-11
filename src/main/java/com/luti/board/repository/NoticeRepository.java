package com.luti.board.repository;

import com.luti.board.entity.Notice;
import com.luti.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 특정 사용자가 작성한 공지글을 페이지 단위로 조회
     *
     * @param user     조회할 작성자
     * @param pageable 페이징 정보
     */
    Page<Notice> findAllByUser(User user, Pageable pageable);

    /**
     * 삭제되지 않은 공지글만 페이지 단위로 조회
     *
     * @param pageable 페이징 정보
     */
    Page<Notice> findAllByDeletedFalse(Pageable pageable);
}
