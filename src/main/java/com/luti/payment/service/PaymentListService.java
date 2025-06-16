package com.luti.payment.service;

import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.entity.PaymentList;
import com.luti.payment.repository.PaymentListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentListService {

    private final PaymentListRepository paymentListRepository;

    /**
     * PaymentList 엔티티를 PaymentListResponseDTO 변환하는 유틸 메서드
     */
    private PaymentListResponseDTO toDto(PaymentList entity) {
        return PaymentListResponseDTO.builder()
                .paymentId(entity.getPaymentId())
                .paymentCd(entity.getPaymentCd())
                .userId(entity.getUserId())
                .totalPrice(entity.getTotalPrice())
                .paymentState(entity.getPaymentState())
                .paymentDate(entity.getPaymentDate())
                .cancelDate(entity.getCancelDate())
                .receiptUrl(entity.getReceiptUrl())
                .impUid(entity.getImpUid())
                .paymentMethodName(
                        entity.getPaymentMethod() != null
                                ? entity.getPaymentMethod().getPaymentMethod()
                                : null
                )
                .build();
    }

    /**
     * 사용자 ID로 결제내역 전체 조회
     */
    public List<PaymentListResponseDTO> findByUserId(Integer userId) {
        return paymentListRepository.findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 결제 방식 코드로 결제내역 조회
     */
    public List<PaymentListResponseDTO> findByPaymentCd(Integer paymentCd) {
        return paymentListRepository.findAll().stream()
                .filter(p -> p.getPaymentCd().equals(paymentCd))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 결제 일자 기준 범위 조회 (ex. 2024-01-01 ~ 2024-01-31)
     */
    public List<PaymentListResponseDTO> findByPaymentDateBetween(LocalDate start, LocalDate end) {
        return paymentListRepository.findByPaymentDateBetween(start, end).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 결제방식 기준으로 가장 최근 결제 1건 조회
     */
    public PaymentListResponseDTO findLatestByPaymentCd(Integer paymentCd) {
        PaymentList latest = paymentListRepository.findTop1ByPaymentCdOrderByPaymentDateDesc(paymentCd);
        return latest != null ? toDto(latest) : null;
    }

    /**
     * 총 결제금액 범위로 결제내역 조회
     */
    public List<PaymentListResponseDTO> findByTotalPriceRange(Integer min, Integer max) {
        return paymentListRepository.findByTotalPriceBetween(min, max).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 결제 취소된 결제내역만 조회
     */
    public List<PaymentListResponseDTO> findAllCancelled() {
        return paymentListRepository.findByCancelDateIsNotNull().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID 기준으로 결제금액 오름차순 정렬 조회
     */
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceAsc(Integer userId) {
        return paymentListRepository.findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .sorted((a, b) -> Integer.compare(
                        a.getTotalPrice() != null ? a.getTotalPrice() : 0,
                        b.getTotalPrice() != null ? b.getTotalPrice() : 0
                ))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID 기준으로 결제금액 내림차순 정렬 조회
     */
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceDesc(Integer userId) {
        return paymentListRepository.findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .sorted((a, b) -> Integer.compare(
                        b.getTotalPrice() != null ? b.getTotalPrice() : 0,
                        a.getTotalPrice() != null ? a.getTotalPrice() : 0
                ))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}
