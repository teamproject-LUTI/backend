package com.luti.payment.repository;

import com.luti.payment.entity.PaymentList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentListRepository extends JpaRepository<PaymentList, Integer> {

    /**
     * 특정 사용자 ID의 모든 결제 내역 조회
     */
    List<PaymentList> findByUserId(Integer userId);

    /**
     * 사용자 ID와 결제 방식 코드로 결제 내역 조회
     */
    List<PaymentList> findByUserIdAndPaymentCd(Integer userId, Integer paymentCd);

    /**
     * 결제일 기준으로 시작일~종료일 사이 결제 내역 조회
     */
    List<PaymentList> findByPaymentDateBetween(LocalDate start, LocalDate end);

    /**
     * 결제 상태별 결제 내역 조회
     */
    List<PaymentList> findByPaymentState(Integer paymentState);

    /**
     * 결제방식 + 결제일 범위 기준 결제 내역 조회
     */
    List<PaymentList> findByPaymentCdAndPaymentDateBetween(Integer paymentCd, LocalDate start, LocalDate end);

    /**
     * 사용자별 가장 최근 결제 내역 5건 조회
     */
    List<PaymentList> findTop5ByUserIdOrderByPaymentDateDesc(Integer userId);

    /**
     * 영수증 URL이 존재하는 결제 내역 조회
     */
    List<PaymentList> findByUserIdAndReceiptUrlIsNotNull(Integer userId);

    /**
     * 특정 금액 이상의 결제 내역 조회
     */
    List<PaymentList> findByTotalPriceGreaterThanEqual(Integer minPrice);

    /**
     * 특정 금액 이하의 결제 내역 조회
     */
    List<PaymentList> findByTotalPriceLessThanEqual(Integer maxPrice);

    /**
     * 결제 금액 범위로 결제 내역 조회
     */
    List<PaymentList> findByTotalPriceBetween(Integer minPrice, Integer maxPrice);

    /**
     * 특정 날짜에 결제된 내역 조회
     */
    List<PaymentList> findByPaymentDate(LocalDate today);

    /**
     * 결제 취소가 발생한 내역만 조회
     */
    List<PaymentList> findByCancelDateIsNotNull();

    /**
     * 사용자별 결제 금액 오름차순 정렬 조회
     */
    List<PaymentList> findByUserIdOrderByTotalPriceAsc(Integer userId);

    /**
     * 사용자별 결제 금액 내림차순 정렬 조회
     */
    List<PaymentList> findByUserIdOrderByTotalPriceDesc(Integer userId);

    /**
     * 결제 상태 + 날짜 범위 기준 결제 내역 조회
     */
    List<PaymentList> findByPaymentStateAndPaymentDateBetween(Integer paymentState, LocalDate start, LocalDate end);

    /**
     * 결제 방식별 가장 최근 결제 1건 조회
     */
    PaymentList findTop1ByPaymentCdOrderByPaymentDateDesc(Integer paymentCd);
}
