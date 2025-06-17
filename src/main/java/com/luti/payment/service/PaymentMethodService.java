package com.luti.payment.service;

import com.luti.payment.dto.PaymentMethodResponseDTO;
import com.luti.payment.entity.PaymentMethod;
import com.luti.payment.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    /**
     * 결제방식 코드로 단건 조회
     */
    public Optional<PaymentMethodResponseDTO> findByCode(Long code) {
        return paymentMethodRepository.findByPaymentMethodId(code)
                .map(PaymentMethodResponseDTO::from);
    }

    /**
     * 결제방식 이름으로 단건 조회
     */
    public Optional<PaymentMethodResponseDTO> findByMethodName(String name) {
        return paymentMethodRepository.findByPaymentMethod(name)
                .map(PaymentMethodResponseDTO::from);
    }

    /**
     * 전체 결제방식 조회 (오름차순 정렬)
     */
    public List<PaymentMethodResponseDTO> findAllAsc() {
        return paymentMethodRepository.findAllByOrderByPaymentCdAsc().stream()
                .map(PaymentMethodResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 결제방식 중복 여부 확인
     */
    public boolean existsByName(String name) {
        return paymentMethodRepository.existsByPaymentMethod(name);
    }

    /**
     * 가장 큰 결제방식 코드 반환 (신규 등록용)
     */
    public Long getNextPaymentCd() {
        PaymentMethod last = paymentMethodRepository.findTopByOrderByPaymentCdDesc();
        return last != null ? last.getPaymentCd() + 1 : 1L;
    }

}
