package com.luti.luti.payment.repository;

import com.luti.luti.payment.entity.PaymentList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentListRepository extends JpaRepository<PaymentList, Integer> {

    // 특정 사용자(loginId)의 모든 결제 내역 조회
    List<PaymentList> findByUser_LoginId(String loginId);

    // 특정 결제 상태(예: "결제완료")인 결제들 조회
    List<PaymentList> findByPaymentState(String state);

    // 특정 사용자 + 특정 결제 상태를 동시에 만족하는 결제 내역 조회
    List<PaymentList> findByUser_LoginIdAndPaymentState(String loginId, String state);

    // 특정 기간 내에 결제된 내역 조회
    List<PaymentList> findByPaymentDateBetween(LocalDate start, LocalDate end);

    // 사용자가 특정 상태의 결제를 한 적이 있는지 여부 확인
    boolean existsByUser_LoginIdAndPaymentState(String loginId, String state);

    // 사용자별 결제 내역을 "결제일 내림차순"으로 정렬해서 조회
    List<PaymentList> findByUser_LoginIdOrderByPaymentDateDesc(String loginId);

    // 사용자별 결제 내역을 "결제일 오름차순"으로 정렬해서 조회
    List<PaymentList> findByUser_LoginIdOrderByPaymentDateAsc(String loginId);

    // 특정 금액 이상으로 결제된 건들 조회 (고액 결제 내역 등)
    List<PaymentList> findByTotalPriceGreaterThanEqual(Integer price);

    // 사용자 결제 내역을 페이지 단위로 조회 (페이징 처리용)
    Page<PaymentList> findByUser_LoginId(String loginId, Pageable pageable);

    // 특정 기간 + 특정 상태의 결제 내역을 동시에 조건으로 검색
    List<PaymentList> findByPaymentDateBetweenAndPaymentState(LocalDate start, LocalDate end, String state);

    // 가장 최근 결제 1건만 조회 (최신 결제 보기 용도)
    PaymentList findTop1ByUser_LoginIdOrderByPaymentDateDesc(String loginId);

    // 특정 결제 수단(예: 카드, 무통장)만 필터링해서 조회
    List<PaymentList> findByPaymentMethod_PaymentCd(Integer paymentCd);

    // JPQL을 이용한 고액 결제 조회 (직접 쿼리 작성)
    @Query("SELECT p FROM PaymentList p WHERE p.totalPrice > :minPrice")
    List<PaymentList> findExpensivePayments(@Param("minPrice") Integer minPrice);
}
