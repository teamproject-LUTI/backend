package com.luti.payment.entity;

import com.luti.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "paymentList")
public class PaymentList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId; // 단일 기본키

    @Column(name = "payment_cd", nullable = false)
    private Integer paymentCd; // 결제방식 코드 (외래키)

    @Column(name = "user_id")
    private Long userId; // 사용자 ID (외래키)

    @Column(name = "totalprice")
    private Integer totalPrice; // 총 결제 금액

    @Column(name = "payment_state")
    private Integer paymentState; // 결제 상태 (0=결제 완료, 1=취소 등)

    @Column(name = "payment_date")
    private LocalDate paymentDate; // 결제 일자

    @Column(name = "cancel_date")
    private LocalDate cancelDate; // 결제 취소 일자

    @Column(name = "receipturl", length = 500)
    private String receiptUrl; // 영수증 URL

    @Column(name = "imp_uid", length = 100)
    private String impUid; // 아임포트 결제 고유값

    // 연관관계 매핑
    @ManyToOne
    @JoinColumn(name = "payment_id", insertable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}
