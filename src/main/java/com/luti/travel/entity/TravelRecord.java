package com.luti.travel.entity;

import com.luti.payment.entity.PaymentList;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "travelRecord")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "travel_record_id", updatable = false, nullable = false)
	private Long travelRecordId;

	// PaymentList 참조 - payment_id로만 연결
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private PaymentList paymentList;

	@Column(name = "travel_title", length = 100)
	private String travelTitle;

	@Column(name = "travel_content", columnDefinition = "TEXT")
	private String travelContent;

	// 편의 메서드들 (필요시)
	public Long getUserId() {
		return paymentList != null ? paymentList.getUserId() : null;
	}

	public Integer getPaymentCd() {
		return paymentList != null ? paymentList.getPaymentCd() : null;
	}

	public Long getPaymentId() {
		return paymentList != null ? paymentList.getPaymentId() : null;
	}
}
