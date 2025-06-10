package com.luti.travel.entity;

import com.luti.auth.entity.User;
import com.luti.payment.antity.PaymentList;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "accomodetail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccomoDetail {

    @Id
    @Column(name = "payment_ownno")
    private Long paymentOwnno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_no")
    public PaymentList paymentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accomo_no")
    public AccomoInfo accomoNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id")
    public User loginId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_cd")
    public PaymentList paymentCd;

    @Column(name = "price")
    private Long price;

    @Column(name = "accomo_start")
    private Date accomoStart;

    @Column(name = "accomo_end")
    private Date accomoEnd;

    @Column(name = "user_count")
    private Long userCount;

    @Column(name = "room_type")
    private String roomType;


}
