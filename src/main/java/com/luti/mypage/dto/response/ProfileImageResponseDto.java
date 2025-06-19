package com.luti.mypage.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 프로필 이미지 관련 응답 DTO
 */
@Getter
@Builder
public class ProfileImageResponseDto {

	private String profileImageUrl;
	private String message;

	public static ProfileImageResponseDto success(String profileImageUrl) {
		return ProfileImageResponseDto.builder()
				.profileImageUrl(profileImageUrl)
				.message("프로필 이미지가 성공적으로 업데이트되었습니다.")
				.build();
	}

	public static ProfileImageResponseDto deleted() {
		return ProfileImageResponseDto.builder()
				.profileImageUrl(null)
				.message("프로필 이미지가 삭제되었습니다.")
				.build();
	}

	public static ProfileImageResponseDto current(String profileImageUrl) {
		return ProfileImageResponseDto.builder()
				.profileImageUrl(profileImageUrl)
				.message("현재 프로필 이미지 정보입니다.")
				.build();
	}
}
