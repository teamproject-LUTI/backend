package com.luti.payment.service;

import com.luti.payment.dto.PaymentListRequestDTO;
import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.entity.PaymentList;
import com.luti.payment.entity.PaymentMethod;
import com.luti.payment.repository.PaymentListRepository;
import com.luti.payment.repository.PaymentMethodRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentListService {

    private final PaymentListRepository paymentListRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    // 결제 정보 저장
    public PaymentListResponseDTO savePayment(PaymentListRequestDTO dto) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentMethodId(dto.getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 결제 방식 ID입니다."));

        PaymentList payment = PaymentList.builder()
                .userId(dto.getUserId())
                .totalPrice(dto.getTotalPrice())
                .paymentState(0) // 0: 결제 완료
                .paymentDate(LocalDateTime.now())
                .impUid(dto.getImpUid())
                .merchantUid(dto.getMerchantUid())
                .build();

        payment.setPaymentMethod(paymentMethod);

        PaymentList saved = paymentListRepository.save(payment);
        return PaymentListResponseDTO.from(saved);
    }

    // 결제 취소 (환불 처리)
    public PaymentListResponseDTO cancelPayment(Long paymentId) {
        PaymentList payment = paymentListRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역을 찾을 수 없습니다."));

        payment.setPaymentState(1); // 1: 환불
        payment.setCancelDate(LocalDateTime.now());

        return PaymentListResponseDTO.from(payment);
    }

    // 사용자 ID로 결제 내역 조회
    public List<PaymentListResponseDTO> findByUserId(Long userId) {
        return paymentListRepository.findByUserId(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 금액 높은 순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceDesc(Long userId) {
        return paymentListRepository.findByUserIdOrderByTotalPriceDesc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 금액 낮은 순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceAsc(Long userId) {
        return paymentListRepository.findByUserIdOrderByTotalPriceAsc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제 상태 필터링 (0=결제, 1=환불)
    public List<PaymentListResponseDTO> findByUserIdAndPaymentState(Long userId, Integer paymentState) {
        return paymentListRepository.findByUserIdAndPaymentState(userId, paymentState).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 기간 필터링 (최근 1달/3달 등)
    public List<PaymentListResponseDTO> findByUserIdAndPaymentDateBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return paymentListRepository.findByUserIdAndPaymentDateBetween(userId, start, end).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제일 기준 최신순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByPaymentDateDesc(Long userId) {
        return paymentListRepository.findByUserIdOrderByPaymentDateDesc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }
}
