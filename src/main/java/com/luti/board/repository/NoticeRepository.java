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
     * 특정 작성자가 올린 공지글을 페이지 단위로 조회합니다.
     *
     * @param author   조회할 작성자(User)
     * @param pageable 페이징 정보(Page 번호, 사이즈, 정렬 등)
     * @return 작성자별 페이징된 공지글 목록
     */
    Page<Notice> findAllByAuthor(User author, Pageable pageable);

    /**
     * 삭제되지 않은(soft delete=false) 공지글만 페이지 단위로 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 삭제되지 않은 공지글의 페이징 목록
     */
    Page<Notice> findAllByDeletedFalse(Pageable pageable);
}
