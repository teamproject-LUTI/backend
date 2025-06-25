package com.luti.board.repository;

import com.luti.board.entity.Ask;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Ask 엔티티에 대한 JPA Repository
 * JpaSpecificationExecutor 추가로 동적 검색 기능 지원
 */
@Repository
public interface AskRepository extends JpaRepository<Ask, Long>, JpaSpecificationExecutor<Ask> {

	/**
	 * 기본 조회
	 */
	@Query("SELECT a FROM Ask a WHERE a.user.userId = :userId ORDER BY a.createdAt DESC")
	Page<Ask> findByUserUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT a FROM Ask a WHERE a.user.userId = :userId ORDER BY a.createdAt ASC")
	Page<Ask> findByUserUserIdOrderByCreatedAtAsc(@Param("userId") Long userId, Pageable pageable);

	/**
	 * 답변 상태 필터 + 최신순
	 */
	@Query("SELECT a FROM Ask a WHERE a.user.userId = :userId AND a.answered = :answered ORDER BY a.createdAt DESC")
	Page<Ask> findByUserUserIdAndAnsweredOrderByCreatedAtDesc(
			@Param("userId") Long userId,
			@Param("answered") Boolean answered,
			Pageable pageable);

	/**
	 * 답변 상태 필터 + 오래된순
	 */
	@Query("SELECT a FROM Ask a WHERE a.user.userId = :userId AND a.answered = :answered ORDER BY a.createdAt ASC")
	Page<Ask> findByUserUserIdAndAnsweredOrderByCreatedAtAsc(
			@Param("userId") Long userId,
			@Param("answered") Boolean answered,
			Pageable pageable);

}