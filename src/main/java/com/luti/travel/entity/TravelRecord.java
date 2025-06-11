package com.luti.travel.entity;

import com.luti.auth.entity.User;
import com.luti.payment.antity.PaymentList;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name= "travelrecord")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelRecord {

    @Id
    @Column(name= "record_no")
    private Long recordNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="payment_no")
    public PaymentList paymentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="login_id")
    private User loginId;

    @Column(name="travel_title",length = 100)
    private String travelTitle;

    @Column(name="travel_content",length = 16383)
    private String travelContent;

}
