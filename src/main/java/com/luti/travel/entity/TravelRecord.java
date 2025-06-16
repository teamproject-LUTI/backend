package com.luti.travel.entity;

import com.luti.payment.entity.PaymentList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "travelrecord")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelRecord {

	@Id
	@Column(name = "record_no")
	private Long recordNo;

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

}
