package com.luti.auth.dto;

import java.util.Map;

/**
 * OAuth2 소셜 로그인 제공자들의 공통 인터페이스
 * 각 제공자별 DTO가 구현해야 하는 공통 메서드를 정의합니다.
 *
 * @author
 */
public interface OAuth2UserInfo {

	/**
	 * 소셜 제공자에서 해당 사용자를 식별하는 고유 ID
	 */
	String getProviderId();

	/**
	 * 소셜 로그인 제공자명 (예: "google", "kakao")
	 */
	String getProvider();

	/**
	 * 사용자의 이메일 주소 (없으면 더미 이메일 생성)
	 */
	String getEmail();

	/**
	 * 사용자의 이름
	 */
	String getName();

	/**
	 * 사용자의 닉네임
	 */
	String getNickname();

	/**
	 * 사용자의 생년월일 (YYYYMMDD 형식, 없으면 null)
	 */
	String getBirthday();

	/**
	 * 사용자의 성별 (남성/여성, 없으면 null)
	 */
	String getGender();

	/**
	 * 사용자의 프로필 이미지 URL
	 */
	String getProfileImageUrl();

	/**
	 * OAuth2 제공자로부터 받은 원본 사용자 속성 맵
	 */
	Map<String, Object> getAttributes();

	/**
	 * 더미 이메일인지 확인
	 */
	default boolean isDummyEmail() {
		String email = getEmail();
		return email != null && (email.contains("@google.local") || email.contains("@kakao.local"));
	}
}
