package com.luti.travel.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.luti.auth.entity.User;
import com.luti.payment.antity.PaymentList;
import com.luti.travel.entity.AccomoDetail;
import com.luti.travel.entity.AccomoInfo;

public interface AccomoDetailRepository extends JpaRepository<AccomoDetail, Long> {
	// 기본 단일 조회

	// paymentOwnno (PK)로 단일 조회
	Optional<AccomoDetail> findByPaymentOwnno(Long paymentOwnno);

	// 사용자 기준
	// 사용자 ID로 전체 예약 목록 조회
	List<AccomoDetail> findByUserId(User userId);

	// 사용자 ID로 예약 목록을 숙박 시작일 내림차순 정렬하여 조회
	List<AccomoDetail> findByuserIdOrderByAccomoStartDesc(User userId);

	// 사용자 ID와 숙박 시작일 범위로 예약 내역 조회
	List<AccomoDetail> findByuserIdAndAccomoStartBetween(User userId, Date start, Date end);

	// 숙소 기준(필요한가?)
	// 특정 숙소(accomoNo)로 예약 내역 전체 조회
	List<AccomoDetail> findByAccomoNo(AccomoInfo accomoNo);

	// 숙소와 방 종류 기준으로 예약 조회
	List<AccomoDetail> findByAccomoNoAndRoomType(AccomoInfo accomoNo, String roomType);

	// 숙소와 최소 인원 수 이상 조건으로 예약 조회
	List<AccomoDetail> findByAccomoNoAndUserCountGreaterThan(AccomoInfo accomoNo, Long userCount);

	// 결제 기준
	// 결제 번호(paymentNo)로 예약 조회
	List<AccomoDetail> findByPaymentNo(PaymentList paymentNo);

	// 결제 코드(paymentCd)로 예약 조회
	List<AccomoDetail> findByPaymentCd(PaymentList paymentCd);

	// 결제 번호 + 결제 코드 조합으로 예약 조회
	List<AccomoDetail> findByPaymentNoAndPaymentCd(PaymentList paymentNo, PaymentList paymentCd);

	// 날짜 기준
	// 정확한 숙박 시작일로 예약 조회
	List<AccomoDetail> findByAccomoStart(Date date);

	// 숙박 시작일이 특정 기간 사이에 있는 예약 조회
	List<AccomoDetail> findByAccomoStartBetween(Date start, Date end);

	// 숙박 시작일이 특정 날짜 이후인 예약 조회
	List<AccomoDetail> findByAccomoStartAfter(Date date);

	// 숙박 종료일이 특정 기간 사이인 예약 조회
	List<AccomoDetail> findByAccomoEndBetween(Date start, Date end);

	// 숙박 시작일 ≤ 시작 조건 AND 숙박 종료일 ≥ 종료 조건
	List<AccomoDetail> findByAccomoStartLessThanEqualAndAccomoEndGreaterThanEqual(Date start, Date end);

	// 가격 기준
	// 특정 가격 이상인 예약 조회
	List<AccomoDetail> findByPriceGreaterThanEqual(Long price);

	// 가격이 특정 범위에 있는 예약 조회
	List<AccomoDetail> findByPriceBetween(Long min, Long max);

	// 인원 수 기준
	// 정확한 인원 수로 예약 조회
	List<AccomoDetail> findByUserCount(Long count);

	// 특정 인원 수 이상인 예약 조회
	List<AccomoDetail> findByUserCountGreaterThanEqual(Long count);

	// 인원 수가 특정 범위 내인 예약 조회
	List<AccomoDetail> findByUserCountBetween(Long min, Long max);

	// 복합 조건
	// 사용자 + 숙소 + 숙박 시작일 범위 조건으로 예약 조회
	List<AccomoDetail> findByuserIdAndAccomoNoAndAccomoStartBetween(
			User userId, AccomoInfo accomoNo, Date start, Date end);

	// 사용자 + 방 종류 + 최소 인원 수 조건으로 예약 조회(필요한가?)
	List<AccomoDetail> findByuserIdAndRoomTypeAndUserCountGreaterThanEqual(
			User userId, String roomType, Long minCount);

}
