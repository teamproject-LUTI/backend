package com.luti.mypage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.luti.mypage.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

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
