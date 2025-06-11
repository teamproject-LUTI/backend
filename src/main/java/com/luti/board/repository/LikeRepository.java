package com.luti.board.repository;

import com.luti.board.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    // 특정 후기글과 유저로 좋아요 여부 확인
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    // 특정 후기글의 좋아요 수
    long countByReviewId(Long reviewId);

    // 특정 리뷰에 대해 내가 좋아요를 눌렀는지 확인
    boolean existsByReview_ReviewNoAndUser_UserId(Long reviewNo, Long userId);

}
