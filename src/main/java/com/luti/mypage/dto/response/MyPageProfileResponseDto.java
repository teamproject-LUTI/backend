package com.luti.mypage.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 마이페이지 프로필 조회 응답 DTO
 */
@Getter
@Builder
public class MyPageProfileResponseDto {

	private BasicInfoDto basicInfo;

	private ContactInfoDto contactInfo;

	@Getter
	@Builder
	public static class BasicInfoDto {

		private String profileImage;

		private String name;

		private String nickname;

		private String birthday;

		private String gender;

	}

	@Getter
	@Builder
	public static class ContactInfoDto {

		private String phoneNumber;

		private String email;

		private String address;

	}

}
