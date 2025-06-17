package com.luti.board.repository;

import com.luti.board.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

