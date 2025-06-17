package com.luti.mypage.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 마이페이지 프로필 정보 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class MyPageProfileUpdateRequestDto {

	// 기본 정보
	private String nickname;
	private String birthday;
	private String gender;

	// 연락처 정보
	private String phoneNumber;
	private String address;

}
