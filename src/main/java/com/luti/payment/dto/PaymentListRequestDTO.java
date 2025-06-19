package com.luti.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PaymentListRequestDTO {

    private Long paymentMethodId;       // 결제 방식 코드
    private Long userId;             // 사용자 ID
    private Integer totalPrice;      // 총 결제 금액
    private LocalDateTime paymentDate;   // 결제 일자/ 시간 (한국 기준)
    private String receiptUrl;       // 영수증 URL
    private String impUid;           // 아임포트 결제 UID
    private String merchantUid;      // 상점 거래 고유번호
}