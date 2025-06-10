package com.luti.luti.payment.service;

import com.luti.luti.payment.entity.PaymentList;
import com.luti.luti.payment.repository.PaymentListRepository;
import com.luti.luti.payment.repository.PaymentMethodRepository;
import com.luti.luti.user.entity.UserInfo;
import com.luti.luti.user.repository.UserInfoRepository;
import com.luti.luti.payment.entity.PaymentMethod;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service



@RequiredArgsConstructor
public class PaymentService {

    private final PaymentListRepository paymentListRepository;
    private final UserInfoRepository userInfoRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    // 결제 저장
    public PaymentList createPayment(String loginId, Integer totalPrice, String state, String desc, Integer paymentCd) {
        UserInfo user = userInfoRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + loginId));

        PaymentMethod method = paymentMethodRepository.findById(paymentCd)
                .orElseThrow(() -> new IllegalArgumentException("결제 수단이 유효하지 않습니다: " + paymentCd));

        PaymentList payment = PaymentList.builder()
                .user(user)
                .totalPrice(totalPrice)
                .paymentState(state)
                .paymentDesc(desc)
                .paymentMethod(method)
                .build();

        return paymentListRepository.save(payment);
    }

    // 특정 사용자 결제 내역 조회 (최신순)
    public List<PaymentList> getPaymentsByUserDesc(String loginId) {
        return paymentListRepository.findByUser_LoginIdOrderByPaymentDateDesc(loginId);
    }

    // 특정 사용자 결제 내역 조회 (오름차순)
    public List<PaymentList> getPaymentsByUserAsc(String loginId) {
        return paymentListRepository.findByUser_LoginIdOrderByPaymentDateAsc(loginId);
    }

    // 가장 최근 결제 1건 조회
    public PaymentList getLatestPayment(String loginId) {
        return paymentListRepository.findTop1ByUser_LoginIdOrderByPaymentDateDesc(loginId);
    }

    // 결제 상태로 필터링 조회
    public List<PaymentList> getPaymentsByState(String state) {
        return paymentListRepository.findByPaymentState(state);
    }

    // 사용자 + 결제 상태로 조회
    public List<PaymentList> getPaymentsByUserAndState(String loginId, String state) {
        return paymentListRepository.findByUser_LoginIdAndPaymentState(loginId, state);
    }

    // 결제 기간 조회
    public List<PaymentList> getPaymentsByDateRange(LocalDate start, LocalDate end) {
        return paymentListRepository.findByPaymentDateBetween(start, end);
    }

    // 결제 상태 + 기간으로 조회
    public List<PaymentList> getPaymentsByDateAndState(LocalDate start, LocalDate end, String state) {
        return paymentListRepository.findByPaymentDateBetweenAndPaymentState(start, end, state);
    }

    // 결제 수단별 조회
    public List<PaymentList> getPaymentsByMethod(Integer paymentCd) {
        return paymentListRepository.findByPaymentMethod_PaymentCd(paymentCd);
    }

    // 고액 결제 조회 (JPQL)
    public List<PaymentList> getExpensivePayments(int minPrice) {
        return paymentListRepository.findExpensivePayments(minPrice);
    }

    // 고액 결제 조회 (메서드 기반)
    public List<PaymentList> getPaymentsOverPrice(int minPrice) {
        return paymentListRepository.findByTotalPriceGreaterThanEqual(minPrice);
    }

    // 페이징된 결제 목록 조회
    public Page<PaymentList> getPaymentsByUserPaged(String loginId, Pageable pageable) {
        return paymentListRepository.findByUser_LoginId(loginId, pageable);
    }

    // 특정 상태 결제 존재 여부 확인
    public boolean hasPaymentWithState(String loginId, String state) {
        return paymentListRepository.existsByUser_LoginIdAndPaymentState(loginId, state);
    }
}
