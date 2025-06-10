package com.luti.luti.payment.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "paymentlist")
public class PaymentList {

    // 결제 번호 (기본 키)
    @Id
    @Column(name = "payment_no")
    private Integer paymentNo;

    // 결제한 사용자
    @ManyToOne
    @JoinColumn(name = "loginID", nullable = false)
    private UserInfo user;

    // 사용된 결제 수단
    @ManyToOne
    @JoinColumn(name = "payment_cd", nullable = false)
    private PaymentMethod paymentMethod;

    // 총 결제 금액
    @Column(name = "totalprice")
    private Integer totalPrice;

    // 결제 상태 (예: 완료, 취소)
    @Column(name = "payment_state")
    private String paymentState;

    // 결제 일자
    @Column(name = "payment_date")
    private LocalDate paymentDate;

    // 영수증 URL
    @Column(name = "receipturl", length = 500)
    private String receiptUrl;

    // 결제 상세 정보 리스트 (1:N)
    @OneToMany(mappedBy = "paymentList")
    private List<PaymentDetail> paymentDetails = new ArrayList<>();
}
