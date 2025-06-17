package com.luti.travel.entity;

import com.luti.auth.entity.User;
import com.luti.payment.entity.PaymentList;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "accomodation_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccomodationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accomodation_detail_id", updatable = false, nullable = false)
    private Long accomodationDetailId;

    @Column(name = "payment_ownno")
    private Long paymentOwnno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    public PaymentList paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accomodation_information_id")
    public AccomodationInformation accomodationInformationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User userId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "payment_cd")
//    public PaymentList paymentCd;

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
