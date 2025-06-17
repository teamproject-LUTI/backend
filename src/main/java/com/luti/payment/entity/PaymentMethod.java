package com.luti.payment.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_method")
public class PaymentMethod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_method_id") // DB PK 컬럼과 일치
	private Long paymentMethodId;

	@Column(name = "payment_cd", unique = true)
	private Long paymentCd; // 카드 = 1, 카카오페이 = 2 등

	@Column(name = "payment_method", length = 50)
	private String paymentMethod; // 카드, 카카오페이, 네이버페이 등

	@OneToMany(mappedBy = "paymentMethod")
	private List<PaymentList> paymentLists = new ArrayList<>(); // 이 결제방식을 참조하는 결제내역들
}
