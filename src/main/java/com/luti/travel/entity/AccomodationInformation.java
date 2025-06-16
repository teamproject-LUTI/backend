package com.luti.travel.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "accomodationInformation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
