package com.luti.board.repository;

import com.luti.board.entity.Comment;
import com.luti.board.entity.Comment.ParentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Polymorphic 댓글 조회용 레포지토리
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 대상(ASK/REVIEW/NOTICE)에 달린 댓글 전체 조회
     * JOIN FETCH를 사용하여 User 정보를 즉시 로딩하여 N+1 문제 해결
     *
     * @param parentType 댓글 대상 타입
     * @param parentId   댓글 대상 ID
     * @return 댓글 목록 (User 정보 포함)
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parentType = :parentType AND c.parentId = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findAllByParentTypeAndParentId(@Param("parentType") ParentType parentType, @Param("parentId") Long parentId);
}