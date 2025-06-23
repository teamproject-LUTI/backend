package com.luti.payment.entity;

import com.luti.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_list")
public class PaymentList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "totalprice")
    private Integer totalPrice;

    @Column(name = "payment_state")
    private Integer paymentState;

    // 결제 일시(한국 시간, 날짜+시간)
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // 취소 일시(한국 시간, 날짜+시간)
    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    @Column(name = "receipturl", length = 500)
    private String receiptUrl;

    @Column(name = "imp_uid", length = 100)
    private String impUid;

    @Column(name = "merchant_uid", length = 100)
    private String merchantUid;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}