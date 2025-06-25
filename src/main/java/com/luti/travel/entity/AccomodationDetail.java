package com.luti.travel.entity;

import com.luti.auth.entity.User;
import com.luti.payment.entity.PaymentList;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "accomodation_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더가 사용할 생성자
@Builder // 빌더 패턴 활성화
public class AccomodationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accomodation_detail_id", updatable = false, nullable = false)
    private Long accomodationDetailId;

    @Column(name = "payment_ownno")
    private Long paymentOwnno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentList paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accomodation_information_id")
    private AccomodationInformation accomodationInformationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

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

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "nights")
    private Integer nights;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "booking_status", length = 20)
    private String bookingStatus;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "confirmed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date confirmedAt;

    // 👇 여기에 추가하세요! 👇
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (bookingStatus == null) {
            bookingStatus = "PENDING";
        }
        if (currency == null) {
            currency = "KRW";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if ("CONFIRMED".equals(bookingStatus) && confirmedAt == null) {
            confirmedAt = new Date();
        }
    }
    // 👆 여기까지 추가! 👆
}