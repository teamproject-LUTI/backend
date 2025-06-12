package com.luti.board.repository;

import com.luti.board.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 삭제되지않은 모든 후기글 조회 */
    List<Review> findAllReview(String delYn);

    /** soft-delete 플래그가 'N'이고, reviewNo가 일치하는 후기글 조회(한 건 조회) */
    Optional<Review> findByReviewNoAndDelYn(Long reviewNo, String delYn);

    /** 관리자용: 삭제된 후기만 조회 */
    @Query("SELECT r FROM Review r WHERE r.delYn = 'Y'")
    List<Review> findAllDeleted();
}
