package com.luti.travel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "accomodation_Information")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더가 사용할 생성자
@Builder // 빌더 패턴 활성화
public class AccomodationInformation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "accomodation_information_id", updatable = false, nullable = false)
	private Long accomodationInformationId;

	@Column(name = "accomo_nm")
	private String accomoNm;

	@Column(name = "post_no")
	private Long postNo;

}