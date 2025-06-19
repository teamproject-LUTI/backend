package com.luti.payment.repository;

import com.luti.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    // PaymentMethodId로 조회
    Optional<PaymentMethod> findByPaymentMethodId(Long paymentMethodId);

    // 결제방식 이름으로 조회
    Optional<PaymentMethod> findByPaymentMethod(String paymentMethod);

    // LIKE 검색
    List<PaymentMethod> findByPaymentMethodContaining(String keyword);

    // 정렬 조회
    List<PaymentMethod> findAllByOrderByPaymentMethodIdAsc();
    List<PaymentMethod> findAllByOrderByPaymentMethodIdDesc();
    List<PaymentMethod> findAllByOrderByPaymentMethodAsc();
    List<PaymentMethod> findAllByOrderByPaymentMethodDesc();

    // 중복 체크
    boolean existsByPaymentMethod(String paymentMethod);
    boolean existsByPaymentMethodId(Long PaymentMethodId);

    // 이름 IN 조회
    List<PaymentMethod> findByPaymentMethodIn(List<String> names);

    // 가장 큰 코드
    PaymentMethod findTopByOrderByPaymentMethodIdDesc();

    // 이름 포함 + 정렬
    List<PaymentMethod> findByPaymentMethodContainingOrderByPaymentMethodAsc(String keyword);
}
