package com.luti.board.repository;

import com.luti.board.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** soft-delete=false 인 후기만 조회 */
    List<Review> findAllByIsDeletedFalse();

    /** soft-delete=true 인 후기만 조회 (관리자용) */
    List<Review> findAllByIsDeletedTrue();

    /**
     * 주어진 reviewId에 해당하는 후기글을 조회합니다.
     * @param reviewId 후기글 식별 번호
     * @return Optional로 감싼 후기글 (없으면 빈 Optional)
     */
    Optional<Review> findByReviewId(Long reviewId);
    /** 나의 리뷰 */
    Page<Review> findByUserUserId(Long userId, Pageable pageable);
    /** 특정 사용자가 작성한 리뷰 총 개수 */
    long countByUserUserId(Long userId);

    /** 내가 쓴 모든 리뷰의 viewCount 합계 */
    @Query("SELECT COALESCE(SUM(r.viewCount), 0) FROM Review r WHERE r.user.userId = :userId")
    long sumViewCountByUserUserId(@Param("userId") Long userId);

    //사용자가 받은 총 좋아요 수
    @Query("SELECT COALESCE(SUM(r.likeCount), 0) FROM Review r WHERE r.user.userId = :userId")
    long sumLikeCountByUserUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)   // ★
    @Query("UPDATE Review r SET r.viewCount = r.viewCount + 1 WHERE r.reviewId = :id")
    int incrementView(@Param("id") Long reviewId);
}

