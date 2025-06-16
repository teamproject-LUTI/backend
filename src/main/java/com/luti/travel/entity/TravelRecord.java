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
  // 개별 복합키 컴포넌트들
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "payment_cd")
	private Integer paymentCd;

	@Column(name = "payment_id")
	private Integer paymentId;

	// PaymentList 참조 (복합키로 매핑)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
			@JoinColumn(name = "payment_cd", referencedColumnName = "payment_cd", insertable = false, updatable = false),
			@JoinColumn(name = "payment_id", referencedColumnName = "payment_id", insertable = false, updatable = false)
	})
  
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
