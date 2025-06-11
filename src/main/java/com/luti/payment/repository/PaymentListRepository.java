package com.luti.payment.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.luti.payment.antity.PaymentList;
import com.luti.payment.antity.id.PaymentListId;

@Repository
public interface PaymentListRepository extends JpaRepository<PaymentList, PaymentListId> {

	// 사용자별 결제내역 조회
	List<PaymentList> findByLoginId(String loginId);

	// 사용자 + 결제방식으로 결제내역 조회
	List<PaymentList> findByLoginIdAndPaymentCd(String loginId, Integer paymentCd);

	// 결제일 기준 기간 조회
	List<PaymentList> findByPaymentDateBetween(LocalDate start, LocalDate end);

	// 결제 상태별 조회 (ex. 결제 완료, 취소 등)
	List<PaymentList> findByPaymentState(Integer paymentState);

	// 결제방식 + 날짜 기준 필터링
	List<PaymentList> findByPaymentCdAndPaymentDateBetween(Integer paymentCd, LocalDate startDate, LocalDate endDate);

	// 특정 사용자의 최신 결제내역 5건 조회
	List<PaymentList> findTop5ByLoginIdOrderByPaymentDateDesc(String loginId);

	// 영수증 URL이 존재하는 결제만 조회
	List<PaymentList> findByLoginIdAndReceiptUrlIsNotNull(String loginId);

	// 총 결제금액이 특정 금액 이상인 내역 조회 (ex. 10만원 이상)
	List<PaymentList> findByTotalPriceGreaterThanEqual(Integer minPrice);

	// 총 결제금액이 특정 금액 이하인 내역 조회 (ex. 20만원 이하)
	List<PaymentList> findByTotalPriceLessThanEqual(Integer maxPrice);

	// 총 결제금액이 특정 범위 내에 있는 내역 조회 (ex. 10만원 ~ 20만원)
	List<PaymentList> findByTotalPriceBetween(Integer minPrice, Integer maxPrice);

	// 오늘 결제된 내역 조회
	List<PaymentList> findByPaymentDate(LocalDate today);

	// 결제취소된 건만 조회
	List<PaymentList> findByCancelDateIsNotNull();

	// 사용자별 결제금액 오름차순 조회
	List<PaymentList> findByLoginIdOrderByTotalPriceAsc(String loginId);

	// 사용자별 결제금액 내림차순 조회
	List<PaymentList> findByLoginIdOrderByTotalPriceDesc(String loginId);

	// 상태 + 날짜 필터 조합
	List<PaymentList> findByPaymentStateAndPaymentDateBetween(Integer paymentState, LocalDate startDate,
			LocalDate endDate);

	// 결제방식별 가장 최근 결제 한 건 조회
	PaymentList findTop1ByPaymentCdOrderByPaymentDateDesc(Integer paymentCd);

}
