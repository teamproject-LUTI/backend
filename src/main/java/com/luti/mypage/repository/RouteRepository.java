package com.luti.mypage.repository;

import com.luti.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.luti.mypage.entity.Route;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {


	/**
	 * 사용자별 루트 목록 조회 (최신순 - route_id 기준)
	 */
	List<Route> findByUserIdOrderByRouteIdDesc(User user);

	/**
	 * 사용자별 루트 개수 조회
	 */
	long countByUserId(User user);

	/**
	 * 특정 사용자의 여행경로에서 user_id를 NULL로 변경
	 *
	 * @param userId 사용자 ID
	 * @return 업데이트된 여행경로 수
	 */
	@Modifying
	@Query(value = "UPDATE route SET user_id = NULL WHERE user_id = :userId", nativeQuery = true)
	int setUserIdToNull(@Param("userId") Long userId);

}
