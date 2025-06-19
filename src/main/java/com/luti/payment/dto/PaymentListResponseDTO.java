package com.luti.payment.dto;

import com.luti.payment.entity.PaymentList;
import com.luti.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentListResponseDTO {

    private Long paymentId;             // 결제 ID
    private Long paymentMethod;         // 결제 방식 ID
    private Long userId;                // 사용자 ID
    private Integer totalPrice;         // 총 결제 금액
    private Integer paymentState;       // 결제 상태
    private LocalDateTime paymentDate;      // 결제 일자 (한국 기준)
    private LocalDateTime cancelDate;       // 결제 취소 일자 (한국 기준)
    private String receiptUrl;          // 영수증 URL
    private String impUid;              // 아임포트 UID
    private String paymentMethodName;   // 결제 방식 이름

    public static PaymentListResponseDTO from(PaymentList entity) {
        PaymentMethod method = entity.getPaymentMethod();

        return PaymentListResponseDTO.builder()
                .paymentId(entity.getPaymentId())
                .paymentMethod(method != null ? method.getPaymentMethodId() : null)
                .userId(entity.getUserId())
                .totalPrice(entity.getTotalPrice())
                .paymentState(entity.getPaymentState())
                .paymentDate(entity.getPaymentDate())
                .cancelDate(entity.getCancelDate())
                .receiptUrl(entity.getReceiptUrl())
                .impUid(entity.getImpUid())
                .paymentMethodName(method != null ? method.getPaymentMethod() : "카드")
                .build();
    }
}
