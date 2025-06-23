package com.luti.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보 DTO
 * Google의 사용자 정보 응답 형식에 특화된 클래스입니다.
 *
 * @author
 */
@Getter
@Builder
public class GoogleUserInfoDto implements OAuth2UserInfo {

	private final String providerId;      // Google의 "sub" 필드
	private final String email;           // Google 이메일
	private final String name;            // Google 이름
	private final String profileImageUrl; // Google 프로필 이미지 URL
	private final Map<String, Object> attributes; // 원본 응답 데이터

	/**
	 * Google OAuth2 응답으로부터 GoogleUserInfoDto 생성
	 *
	 * @param attributes Google OAuth2에서 반환하는 사용자 정보 Map
	 * @return GoogleUserInfoDto 객체
	 */
	public static GoogleUserInfoDto from(Map<String, Object> attributes) {
		return GoogleUserInfoDto.builder()
				.providerId(String.valueOf(attributes.get("sub"))) // Google 고유 ID
				.email((String) attributes.get("email"))
				.name((String) attributes.get("name"))
				.profileImageUrl((String) attributes.get("picture"))
				.attributes(attributes)
				.build();
	}

	@Override
	public String getProviderId() {
		return providerId;
	}

	@Override
	public String getProvider() {
		return "google";
	}

	@Override
	public String getEmail() {
		// Google은 항상 이메일을 제공하므로 더미 이메일 생성하지 않음
		return email;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNickname() {
		// Google은 별도 닉네임이 없으므로 이름 사용
		return name;
	}

	@Override
	public String getBirthday() {
		// Google은 기본적으로 생년월일 정보를 제공하지 않음
		return null;
	}

	@Override
	public String getGender() {
		// Google은 기본적으로 성별 정보를 제공하지 않음
		return null;
	}

	@Override
	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
