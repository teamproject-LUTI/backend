package com.luti.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.luti.payment.antity.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

	// 결제방식 이름으로 조회 (정확히 일치)
	Optional<PaymentMethod> findByPaymentMethod(String paymentMethod);

	// 결제방식 이름 일부로 검색 (LIKE 검색)
	List<PaymentMethod> findByPaymentMethodContaining(String keyword);

	// 결제방식 코드 목록으로 조회 (IN 조건)
	List<PaymentMethod> findByPaymentCdIn(List<Integer> codes);

	// 결제방식 코드 기준 오름차순 정렬 전체 조회
	List<PaymentMethod> findAllByOrderByPaymentCdAsc();

	// 결제방식 이름 기준 오름차순 정렬 전체 조회
	List<PaymentMethod> findAllByOrderByPaymentMethodAsc();

	// 결제방식 코드 기준 내림차순 정렬
	List<PaymentMethod> findAllByOrderByPaymentCdDesc();

	// 결제방식 이름 기준 내림차순 정렬
	List<PaymentMethod> findAllByOrderByPaymentMethodDesc();

	// 결제방식 중복 존재 여부 확인
	boolean existsByPaymentMethod(String paymentMethod);

	// 특정 코드의 결제방식 존재 여부 확인
	boolean existsByPaymentCd(Integer paymentCd);

	// 이름 리스트로 조회 (예: "카드", "현금" 등)
	List<PaymentMethod> findByPaymentMethodIn(List<String> names);

	// 가장 큰 코드값 1건 조회 (신규 등록 시 사용)
	PaymentMethod findTopByOrderByPaymentCdDesc();

	// 이름 포함 검색 + 오름차순 정렬
	List<PaymentMethod> findByPaymentMethodContainingOrderByPaymentMethodAsc(String keyword);

}
