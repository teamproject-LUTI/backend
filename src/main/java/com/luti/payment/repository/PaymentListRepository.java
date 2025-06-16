package com.luti.payment.repository;

import com.luti.payment.entity.PaymentList;
import com.luti.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentListRepository extends JpaRepository<PaymentList, Long> {

    // 사용자 ID로 전체 결제 내역 조회
    List<PaymentList> findByUserId(Long userId);

    // 결제 방식 (연관 엔티티 기준)으로 조회
    List<PaymentList> findByPaymentMethod(PaymentMethod paymentMethod);

    // 결제 일자 범위 조회
    List<PaymentList> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

    // 결제 상태로 조회 (예: 0=결제, 1=환불)
    List<PaymentList> findByPaymentState(Integer paymentState);

    // 사용자별 금액 오름차순/내림차순 정렬
    List<PaymentList> findByUserIdOrderByTotalPriceAsc(Long userId);
    List<PaymentList> findByUserIdOrderByTotalPriceDesc(Long userId);
}
