package com.luti.board.repository;

import com.luti.board.entity.Comment;
import com.luti.board.entity.Comment.ParentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Polymorphic 댓글 조회용 레포지토리
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 대상(ASK/REVIEW)에 달린 댓글 전체 조회
     *
     * @param parentType 댓글 대상 타입
     * @param parentId   댓글 대상 ID
     */
    List<Comment> findAllByParentTypeAndParentId(ParentType parentType, Long parentId);
}
