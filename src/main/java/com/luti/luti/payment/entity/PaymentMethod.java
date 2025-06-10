package com.luti.luti.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "paymentmethod")
public class PaymentMethod {

    // 결제 코드 (기본 키)
    @Id
    @Column(name = "payment_cd")
    private Integer paymentCd;

    // 결제 수단명 (예: 카드, 무통장)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    // 이 결제수단을 사용하는 결제 내역들 (양방향 매핑)
    @OneToMany(mappedBy = "paymentMethod")
    private List<PaymentList> paymentLists = new ArrayList<>();
}

