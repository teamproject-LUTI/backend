package com.luti.board.repository;

import com.luti.board.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * 좋아요 취소(삭제) 처리
     */
    void deleteByReviewReviewIdAndUserUserId(Long reviewId, Long userId);

    /**
     * 특정 사용자가 특정 후기글에 좋아요를 눌렀는지 여부를 조회
     *
     * @param reviewId 후기글 식별자
     * @param userId   사용자 식별자
     * @return 존재 여부(true: 좋아요 O, false: 좋아요 X)
     */
    boolean existsByReviewReviewIdAndUserUserId(Long reviewId, Long userId);

    /**
     * 특정 후기글의 전체 좋아요 개수를 조회
     *
     * @param reviewId 후기글 식별자
     * @return 좋아요 개수
     */
    long countByReviewReviewId(Long reviewId);

    /**내가 올린 모든 리뷰에 대한 좋아요 총합 */
    long countByReviewUserUserId(Long userId);


    /**
     * 사용자가 누른 전체 좋아요 레코드
     */
    List<Like> findAllByUserUserId(Long userId);

}
