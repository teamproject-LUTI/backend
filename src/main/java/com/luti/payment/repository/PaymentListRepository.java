package com.luti.payment.repository;

import com.luti.payment.entity.PaymentList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentListRepository extends JpaRepository<PaymentList, Long> {

    // 전체 결제 내역 (사용자 기준)
    List<PaymentList> findByUserId(Long userId);

    // 총 결제 금액 기준 정렬 (높은 순)
    List<PaymentList> findByUserIdOrderByTotalPriceDesc(Long userId);

    // 총 결제 금액 기준 정렬 (낮은 순)
    List<PaymentList> findByUserIdOrderByTotalPriceAsc(Long userId);

    // 최근 날짜 기준 정렬 (최신순)
    List<PaymentList> findByUserIdOrderByPaymentDateDesc(Long userId);

    // 사용자 ID + 결제 상태 조회 (예: 내 환불된 내역만 보기)
    List<PaymentList> findByUserIdAndPaymentState(Long userId, Integer paymentState);

    // 사용자 + 결제일 범위 필터링 (최근 1달/3달 등)
    List<PaymentList> findByUserIdAndPaymentDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // 결제 상태 + 날짜 범위로 전체 사용자 결제 내역 조회 (관리자용)
    List<PaymentList> findByPaymentStateAndPaymentDateBetween(
            Integer paymentState, LocalDateTime start, LocalDateTime end);

    // 결제 상태로 전체 조회 (관리자용)
    List<PaymentList> findByPaymentState(Integer paymentState);

}
