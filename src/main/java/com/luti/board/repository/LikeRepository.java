package com.luti.board.repository;

import com.luti.board.entity.Like;
import com.luti.board.entity.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 특정 사용자가 좋아요 누른 리뷰들을 조인으로 한 번에 가져오기 (검색 조건 없이)
     */
    @Query("SELECT r FROM Like l JOIN l.review r WHERE l.user.userId = :userId")
    List<Review> findLikedReviewsByUserId(@Param("userId") Long userId);

    /**
     * 페이지네이션과 정렬을 적용한 좋아요 누른 리뷰 조회 (검색 조건 없음)
     * 최신 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user " +
           "WHERE l.user.userId = :userId " +
           "ORDER BY l.createdAt DESC")
    Page<Like> findLikedReviewsByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 페이지네이션과 정렬을 적용한 좋아요 누른 리뷰 조회 (검색 조건 없음)
     * 오래된 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user " +
           "WHERE l.user.userId = :userId " +
           "ORDER BY l.createdAt ASC")
    Page<Like> findLikedReviewsByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 제목으로 검색 + 최신 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user " +
           "WHERE l.user.userId = :userId " +
           "AND r.title LIKE %:keyword% " +
           "ORDER BY l.createdAt DESC")
    Page<Like> findLikedReviewsByUserIdAndTitleContainingOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 제목으로 검색 + 오래된 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user " +
           "WHERE l.user.userId = :userId " +
           "AND r.title LIKE %:keyword% " +
           "ORDER BY l.createdAt ASC")
    Page<Like> findLikedReviewsByUserIdAndTitleContainingOrderByCreatedAtAsc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 내용으로 검색 + 최신 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user " +
           "WHERE l.user.userId = :userId " +
           "AND r.content LIKE %:keyword% " +
           "ORDER BY l.createdAt DESC")
    Page<Like> findLikedReviewsByUserIdAndContentContainingOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 내용으로 검색 + 오래된 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user " +
           "WHERE l.user.userId = :userId " +
           "AND r.content LIKE %:keyword% " +
           "ORDER BY l.createdAt ASC")
    Page<Like> findLikedReviewsByUserIdAndContentContainingOrderByCreatedAtAsc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 작성자로 검색 + 최신 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user u " +
           "WHERE l.user.userId = :userId " +
           "AND (u.name LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
           "ORDER BY l.createdAt DESC")
    Page<Like> findLikedReviewsByUserIdAndAuthorContainingOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 작성자로 검색 + 오래된 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user u " +
           "WHERE l.user.userId = :userId " +
           "AND (u.name LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
           "ORDER BY l.createdAt ASC")
    Page<Like> findLikedReviewsByUserIdAndAuthorContainingOrderByCreatedAtAsc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 제목, 내용, 작성자 통합 검색 + 최신 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user u " +
           "WHERE l.user.userId = :userId " +
           "AND (r.title LIKE %:keyword% OR r.content LIKE %:keyword% OR u.name LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
           "ORDER BY l.createdAt DESC")
    Page<Like> findLikedReviewsByUserIdAndAllFieldsContainingOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 제목, 내용, 작성자 통합 검색 + 오래된 좋아요순 정렬
     */
    @Query("SELECT l FROM Like l JOIN FETCH l.review r JOIN FETCH r.user u " +
           "WHERE l.user.userId = :userId " +
           "AND (r.title LIKE %:keyword% OR r.content LIKE %:keyword% OR u.name LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
           "ORDER BY l.createdAt ASC")
    Page<Like> findLikedReviewsByUserIdAndAllFieldsContainingOrderByCreatedAtAsc(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
