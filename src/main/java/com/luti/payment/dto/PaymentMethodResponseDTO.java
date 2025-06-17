package com.luti.payment.dto;

import com.luti.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponseDTO {

    private Long paymentId;
    private Long paymentCd;
    private String paymentMethod;

    public static PaymentMethodResponseDTO from(PaymentMethod entity) {
        return PaymentMethodResponseDTO.builder()
                .paymentCd(entity.getPaymentCd())
                .paymentMethod(entity.getPaymentMethod())
                .build();
    }
}
