package com.luti.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentListRequestDTO {

    private Long paymentCd;       // 결제 방식 코드
    private Long userId;             // 사용자 ID
    private Integer totalPrice;      // 총 결제 금액
    private LocalDate paymentDate;   // 결제 일자
    private String receiptUrl;       // 영수증 URL
    private String impUid;           // 아임포트 결제 UID
    private String merchantUid;      // 상점 거래 고유번호
}