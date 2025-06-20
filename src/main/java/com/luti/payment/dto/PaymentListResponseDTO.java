package com.luti.payment.dto;

import com.luti.payment.entity.PaymentList;
import com.luti.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
    private String paymentDate;         // 결제 일자 (String, 한국시간 yyyy-MM-dd HH:mm:ss)
    private String cancelDate;          // 결제 취소 일자 (String, 한국시간 yyyy-MM-dd HH:mm:ss)
    private String receiptUrl;          // 영수증 URL
    private String impUid;              // 아임포트 UID
    private String paymentMethodName;   // 결제 방식 이름

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String formatKorean(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return ZonedDateTime.of(dateTime, ZoneId.of("Asia/Seoul")).format(formatter);
    }

    public static PaymentListResponseDTO from(PaymentList entity) {
        PaymentMethod method = entity.getPaymentMethod();

        return PaymentListResponseDTO.builder()
                .paymentId(entity.getPaymentId())
                .paymentMethod(method != null ? method.getPaymentMethodId() : null)
                .userId(entity.getUserId())
                .totalPrice(entity.getTotalPrice())
                .paymentState(entity.getPaymentState())
                .paymentDate(formatKorean(entity.getPaymentDate()))
                .cancelDate(formatKorean(entity.getCancelDate()))
                .receiptUrl(entity.getReceiptUrl())
                .impUid(entity.getImpUid())
                .paymentMethodName(method != null ? method.getPaymentMethod() : "카드")
                .build();
    }
}
