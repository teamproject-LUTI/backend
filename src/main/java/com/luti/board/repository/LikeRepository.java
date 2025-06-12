package com.luti.board.repository;

import com.luti.board.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**  특정 게시물과 유저로 좋아요 여부 확인*/
    boolean existsByReview_ReviewNoAndUser_UserId(Long reviewNo, Long userId);

    /** 특정 게시물의 좋아요 수*/
    long countByReviewId(Long reviewNo);


}
