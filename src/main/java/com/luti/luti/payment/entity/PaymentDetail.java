package com.luti.luti.payment.entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "paymentdetail")
public class PaymentDetail {

    // 결제 상세 번호 (기본 키)
    @Id
    @Column(name = "payment_ownno")
    private Integer paymentOwnNo;

    // 어떤 결제 내역에 속하는지
    @ManyToOne
    @JoinColumn(name = "payment_no", nullable = false)
    private PaymentList paymentList;

    // 숙소 이름
    @Column(name = "accomo_nm", length = 100)
    private String accommodationName;

    // 숙소 우편번호
    @Column(name = "post_no")
    private Integer postNo;

    // 숙박 1일당 가격
    @Column(name = "price")
    private Integer price;

    // 숙박 시작일
    @Column(name = "accomo_start")
    private LocalDate accommodationStart;

    // 숙박 종료일
    @Column(name = "accomo_end")
    private LocalDate accommodationEnd;

    // 총 숙박 가격
    @Column(name = "price2")
    private Integer price2;

    // 인원 수
    @Column(name = "user_count")
    private Integer userCount;

    // 객실 타입
    @Column(name = "room_type", length = 50)
    private String roomType;

    // 결제한 사용자
    @ManyToOne
    @JoinColumn(name = "loginID", nullable = false)
    private UserInfo user;

    // 사용된 결제 수단
    @ManyToOne
    @JoinColumn(name = "payment_cd", nullable = false)
    private PaymentMethod paymentMethod;
}
