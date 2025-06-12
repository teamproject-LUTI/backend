package com.luti.board.repository;

import com.luti.board.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 삭제되지않은 모든 후기글 조회 */
    List<Review> findAllDeletedFalseReview();

    /**
     * 주어진 reviewNo에 해당하는 후기글을 조회합니다.
     * @param reviewNo 후기글 식별 번호
     * @return Optional로 감싼 후기글 (없으면 빈 Optional)
     */
    Optional<Review> findByReviewNo(Long reviewNo);

    // 관리자 용: 삭제된 글만 보고 싶다면
    //soft-delete 자동 필터링(@Where) 적용 시에는 굳이 findAllByIsDeletedFalse가 필요 없슴
    List<Review> findAllByIsDeletedTrue();
}
