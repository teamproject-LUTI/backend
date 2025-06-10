package com.luti.luti.payment.antity;

import com.luti.luti.auth.entity.User;
import com.luti.luti.payment.antity.id.PaymentListId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "paymentlist")
@IdClass(PaymentListId.class)
public class PaymentList {

    @Id
    @Column(name = "loginId", length = 50, nullable = false)
    private String loginId; // 사용자 ID (외래키, 복합키 구성 요소)

    @Id
    @Column(name = "payment_cd", nullable = false)
    private Integer paymentCd; // 결제방식 코드 (외래키, 복합키 구성 요소)

    @Id
    @Column(name = "payment_no", nullable = false)
    private Integer paymentNo; // 결제 번호 (복합키 구성 요소, 개별 결제 식별용)

    @Column(name = "totalprice")
    private Integer totalPrice; // 총 결제 금액

    @Column(name = "payment_state")
    private Integer paymentState; // 결제 상태 (예: 0=결제 완료, 1=취소 등)

    @Column(name = "payment_date")
    private LocalDate paymentDate; // 결제 일자

    @Column(name = "cancel_date")
    private LocalDate cancelDate; // 결제 취소 일자

    @Column(name = "receipturl", length = 500)
    private String receiptUrl; // 영수증 URL (결제 완료 시 발급되는 외부 링크 등)

    @ManyToOne
    @JoinColumn(name = "payment_cd", insertable = false, updatable = false)
    private PaymentMethod paymentMethod; // 결제방식 엔티티와의 연관관계 (payment_cd 기준)

    @ManyToOne
    @JoinColumn(name = "login_id", referencedColumnName = "loginId", insertable = false, updatable = false)
    private User user;
}
