package com.luti.payment.dto;

import com.luti.payment.entity.PaymentList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentListResponseDTO {

  private Long paymentCd;        // 결제 ID (PK)
    private Long userId;           // 사용자 ID
    private Integer totalPrice;       // 총 결제 금액
    private Integer paymentState;     // 결제 상태
    private LocalDate paymentDate;    // 결제 일자
    private LocalDate cancelDate;     // 결제 취소 일자
    private String receiptUrl;        // 영수증 URL
    private String impUid;            // 아임포트 UID
    private String paymentMethodName; // 결제 방식 이름 (join 된 정보)

    public static PaymentListResponseDTO from(PaymentList entity) {
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
                .paymentMethodName(entity.getPaymentMethod() != null ? entity.getPaymentMethod().getPaymentMethod() : null)
                .build();
    }
}
