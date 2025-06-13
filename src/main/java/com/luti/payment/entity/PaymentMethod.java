package com.luti.payment.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "paymentMethod")
public class PaymentMethod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_method_id",updatable = false, nullable = false)
	private Long paymentMethodId; // 결제방식 ID (기본키)

	@Column(name = "payment_cd")
	private Integer paymentCd; // 결제방식코드 (예: 1=카드, 2=현금 등)

	@Column(name = "payment_method", length = 50)
	private String paymentMethod; // 결제방식 이름 (예: "신용카드", "현금")

	@OneToMany(mappedBy = "paymentMethod")
	private List<PaymentList> paymentLists = new ArrayList<>();  // 이 결제방식을 참조하는 모든 결제내역들



}
