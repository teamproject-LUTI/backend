package com.luti.travel.repository;

import com.luti.auth.entity.User;
import com.luti.payment.entity.PaymentList;
import com.luti.travel.entity.AccomodationDetail;
import com.luti.travel.entity.AccomodationInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AccomodationDetailRepository extends JpaRepository<AccomodationDetail, Long> {

    // 기본 단일 조회 - ID로 예약 조회
    Optional<AccomodationDetail> findByAccomodationDetailId(Long accomodationDetailId);

    // 사용자 기준 조회
    // 사용자 ID로 전체 예약 목록 조회
    List<AccomodationDetail> findByUserId(User userId);

    // 사용자 ID로 예약 목록을 숙박 시작일 내림차순 정렬하여 조회 (최신순)
    List<AccomodationDetail> findByUserIdOrderByAccomoStartDesc(User userId);

    // 사용자 ID와 숙박 시작일 범위로 예약 내역 조회
    List<AccomodationDetail> findByUserIdAndAccomoStartBetween(User userId, Date start, Date end);

    // 결제 정보가 있는 확정된 예약만 조회
    List<AccomodationDetail> findByUserIdAndPaymentIdIsNotNull(User userId);

    // 결제 정보가 없는 임시 예약만 조회 (결제 대기중)
    List<AccomodationDetail> findByUserIdAndPaymentIdIsNull(User userId);

    // 숙소 기준 조회
    // 특정 숙소(accomoId)로 예약 내역 전체 조회
    List<AccomodationDetail> findByAccomodationInformationId(AccomodationInformation accomodationInformationId);

    // 숙소와 방 종류 기준으로 예약 조회
    List<AccomodationDetail> findByAccomodationInformationIdAndRoomType(AccomodationInformation accomodationInformationId, String roomType);

    // 결제 기준 조회
    // 결제 번호(paymentId)로 예약 조회
    List<AccomodationDetail> findByPaymentId(PaymentList paymentId);

    // 날짜 기준 조회
    // 정확한 숙박 시작일로 예약 조회
    List<AccomodationDetail> findByAccomoStart(Date date);

    // 숙박 시작일이 특정 기간 사이에 있는 예약 조회
    List<AccomodationDetail> findByAccomoStartBetween(Date start, Date end);

    // 숙박 시작일이 특정 날짜 이후인 예약 조회
    List<AccomodationDetail> findByAccomoStartAfter(Date date);

    // 숙박 종료일이 특정 기간 사이인 예약 조회
    List<AccomodationDetail> findByAccomoEndBetween(Date start, Date end);

    // 현재 진행중인 숙박 조회 (숙박 시작일 ≤ 현재 AND 숙박 종료일 ≥ 현재)
    List<AccomodationDetail> findByAccomoStartLessThanEqualAndAccomoEndGreaterThanEqual(Date start, Date end);

    // 가격 기준 조회
    // 특정 가격 이상인 예약 조회
    List<AccomodationDetail> findByPriceGreaterThanEqual(Long price);

    // 가격이 특정 범위에 있는 예약 조회
    List<AccomodationDetail> findByPriceBetween(Long min, Long max);

    // 인원 수 기준 조회
    // 정확한 인원 수로 예약 조회
    List<AccomodationDetail> findByUserCount(Long count);

    // 특정 인원 수 이상인 예약 조회
    List<AccomodationDetail> findByUserCountGreaterThanEqual(Long count);

    // 복합 조건 조회
    // 사용자 + 숙소 + 숙박 시작일 범위 조건으로 예약 조회
    List<AccomodationDetail> findByUserIdAndAccomodationInformationIdAndAccomoStartBetween(
            User userId, AccomodationInformation accomodationInformationId, Date start, Date end);

    // 사용자 + 방 종류 + 최소 인원 수 조건으로 예약 조회
    List<AccomodationDetail> findByUserIdAndRoomTypeAndUserCountGreaterThanEqual(
            User userId, String roomType, Long minCount);

    // 결제 대기중인 예약 삭제 (특정 시간 후 자동 삭제용)
    void deleteByPaymentIdIsNullAndAccomoStartBefore(Date cutoffDate);

    // 특정 사용자의 결제 대기중 예약 개수 조회
    long countByUserIdAndPaymentIdIsNull(User userId);
}