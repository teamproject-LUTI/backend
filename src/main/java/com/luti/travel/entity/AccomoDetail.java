package com.luti.travel.entity;

import java.util.Date;

import com.luti.payment.antity.PaymentList;

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
@Table(name = "accomodetail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccomoDetail {

	@Id
	@Column(name = "payment_ownno")
	private Long paymentOwnno;

	// 개별 복합키 컴포넌트들
	@Column(name = "login_id")
	private String loginId;

	@Column(name = "payment_cd")
	private Integer paymentCd;

	@Column(name = "payment_no")
	private Integer paymentNo;

	// PaymentList 참조 (복합키로 매핑)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "login_id", referencedColumnName = "login_Id", insertable = false, updatable = false),
			@JoinColumn(name = "payment_cd", referencedColumnName = "payment_cd", insertable = false, updatable = false),
			@JoinColumn(name = "payment_no", referencedColumnName = "payment_no", insertable = false, updatable = false)
	})
	private PaymentList paymentList;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accomo_no")
	public AccomoInfo accomoNo;

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
