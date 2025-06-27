package com.luti.mypage.repository;

import com.luti.auth.entity.User;
import com.luti.mypage.entity.TravelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelRecordRepository extends JpaRepository<TravelRecord, Long> {

	/**
	 * 사용자별 여행 기록 조회 (ID 역순) - Route와 동일한 패턴
	 */
	List<TravelRecord> findByUserIdOrderByTravelRecordIdDesc(User userId);

	/**
	 * 특정 사용자의 특정 여행 기록 조회 - Route와 동일한 패턴
	 */
	Optional<TravelRecord> findByTravelRecordIdAndUserId(Long travelRecordId, User userId);

	/**
	 * 사용자별 여행 기록 개수 - Route와 동일한 패턴
	 */
	long countByUserId(User userId);

	/**
	 * 결제 ID로 여행 기록 조회
	 */
	Optional<TravelRecord> findByPaymentId(Long paymentId);

	/**
	 * 사용자별 최근 N개 여행 기록 조회 - User 객체 사용
	 */
	@Query("SELECT tr FROM TravelRecord tr WHERE tr.userId = :user ORDER BY tr.travelRecordId DESC LIMIT :limit")
	List<TravelRecord> findRecentTravelRecords(@Param("user") User user, @Param("limit") int limit);

	/**
	 * 결제 정보로 여행 기록 존재 여부 확인 - User 객체 사용
	 */
	boolean existsByUserIdAndPaymentCdAndPaymentId(User userId, Integer paymentCd, Long paymentId);
}