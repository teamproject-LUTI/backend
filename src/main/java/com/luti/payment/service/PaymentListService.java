package com.luti.payment.service;

import com.luti.payment.dto.PaymentListRequestDTO;
import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.entity.PaymentList;
import com.luti.payment.entity.PaymentMethod;
import com.luti.payment.repository.PaymentListRepository;
import com.luti.payment.repository.PaymentMethodRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        // 연관관계 객체 없이 먼저 build
        PaymentList payment = PaymentList.builder()
                .userId(dto.getUserId())
                .totalPrice(dto.getTotalPrice())
                .paymentState(0) // 0: 결제완료
                .paymentDate(dto.getPaymentDate())
                .impUid(dto.getImpUid())
                .merchantUid(dto.getMerchantUid())
                .build();

        // 연관관계 주입 (중요!)
        payment.setPaymentMethod(paymentMethod);

        PaymentList saved = paymentListRepository.save(payment);

        return PaymentListResponseDTO.from(saved);
    }

    // 결제 취소 (환불 처리)
    public PaymentListResponseDTO cancelPayment(Long paymentId) {
        PaymentList payment = paymentListRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역을 찾을 수 없습니다."));

        payment.setPaymentState(1); // 1: 환불
        payment.setCancelDate(LocalDate.now());

        return PaymentListResponseDTO.from(payment);
    }

    // 사용자 ID로 결제 내역 조회
    public List<PaymentListResponseDTO> findByUserId(Long userId) {
        return paymentListRepository.findByUserId(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }
}
