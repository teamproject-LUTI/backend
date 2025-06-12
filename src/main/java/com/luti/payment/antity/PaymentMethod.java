package com.luti.payment.antity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "paymentmethod")
public class PaymentMethod {

	@Id
	@Column(name = "payment_cd")
	private Integer paymentCd; // 결제방식코드 (예: 1=카드, 2=현금 등)

	@Column(name = "payment_method", length = 50)
	private String paymentMethod; // 결제방식 이름 (예: "신용카드", "현금")

	@OneToMany(mappedBy = "paymentMethod")
	private List<PaymentList> paymentLists = new ArrayList<>();  // 이 결제방식을 참조하는 모든 결제내역들

}
