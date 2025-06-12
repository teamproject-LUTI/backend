package com.luti.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 설명: OAuth2 소셜 로그인 제공자로부터 받은 사용자 정보를 애플리케이션의 공통 형식으로 통합하는 DTO(Data Transfer Object)입니다.
 * 각기 다른 소셜 제공자의 응답 형태를 표준화하여 일관된 사용자 정보 처리를 가능하게 합니다.
 *
 * @author
 */
@Getter
@Builder
public class OAuth2UserInfoDto {

	private String providerId;      // 소셜 제공자(예: Google, Kakao)에서 해당 사용자를 식별하는 고유 ID

	private String provider;        // 소셜 로그인 제공자명 (예: "google", "kakao", "naver")

	private String email;           // 사용자의 이메일 주소

	private String name;            // 사용자의 이름 또는 닉네임

	private String profileImageUrl; // 사용자의 프로필 이미지 URL

	private Map<String, Object> attributes; // OAuth2 제공자로부터 받은 원본 사용자 속성(attributes) 맵

	/**
	 * 설명: Google OAuth2 제공자로부터 받은 원본 속성(attributes) 맵을 이용하여 OAuth2UserInfoDto 객체를 생성합니다.
	 * Google 응답 형식에 맞춰 providerId, email, name, profileImageUrl을 추출합니다.
	 *
	 * @param attributes Google OAuth2에서 반환하는 사용자 정보가 담긴 Map 객체.
	 * @return OAuth2UserInfoDto Google 사용자 정보로 초기화된 OAuth2UserInfoDto 객체.
	 * @author
	 */
	public static OAuth2UserInfoDto ofGoogle(Map<String, Object> attributes) {
		return OAuth2UserInfoDto.builder()
				.providerId(String.valueOf(attributes.get("sub"))) // Google의 고유 ID는 "sub" 필드에 있습니다.
				.provider("google")
				.email((String)attributes.get("email"))
				.name((String)attributes.get("name"))
				.profileImageUrl((String)attributes.get("picture"))
				.attributes(attributes) // 원본 속성 맵을 저장합니다.
				.build();
	}

	/**
	 * 설명: Kakao OAuth2 제공자로부터 받은 원본 속성(attributes) 맵을 이용하여 OAuth2UserInfoDto 객체를 생성합니다.
	 * Kakao 응답 형식에 맞춰 providerId, email, name, profileImageUrl을 추출합니다.
	 *
	 * @param attributes Kakao OAuth2에서 반환하는 사용자 정보가 담긴 Map 객체.
	 * @return OAuth2UserInfoDto Kakao 사용자 정보로 초기화된 OAuth2UserInfoDto 객체.
	 * @author
	 */
	public static OAuth2UserInfoDto ofKakao(Map<String, Object> attributes) {
		// Kakao 사용자 정보는 'kakao_account' 및 'profile' 중첩 Map에 포함되어 있습니다.
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");

		return OAuth2UserInfoDto.builder()
				.providerId(String.valueOf(attributes.get("id"))) // Kakao의 고유 ID는 최상위 "id" 필드에 있습니다.
				.provider("kakao")
				.email((String)kakaoAccount.get("email"))
				.name((String)profile.get("nickname"))
				.profileImageUrl((String)profile.get("profile_image_url"))
				.attributes(attributes) // 원본 속성 맵을 저장합니다.
				.build();
	}

	/**
	 * 설명: Naver OAuth2 제공자로부터 받은 원본 속성(attributes) 맵을 이용하여 OAuth2UserInfoDto 객체를 생성합니다.
	 * Naver 응답 형식에 맞춰 providerId, email, name, profileImageUrl을 추출합니다.
	 *
	 * @param attributes Naver OAuth2에서 반환하는 사용자 정보가 담긴 Map 객체.
	 * @return OAuth2UserInfoDto Naver 사용자 정보로 초기화된 OAuth2UserInfoDto 객체.
	 */
	public static OAuth2UserInfoDto ofNaver(Map<String, Object> attributes) {
		// Naver 사용자 정보는 'response' 중첩 Map에 포함되어 있습니다.
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");

		return OAuth2UserInfoDto.builder()
				.providerId((String)response.get("id")) // Naver의 고유 ID는 'response' 안의 "id" 필드에 있습니다.
				.provider("naver")
				.email((String)response.get("email"))
				.name((String)response.get("name"))
				.profileImageUrl((String)response.get("profile_image"))
				.attributes(attributes) // 원본 속성 맵을 저장합니다.
				.build();
	}

	/**
	 * 설명: 소셜 로그인 제공자(registrationId)에 따라 적절한 팩토리 메서드를 호출하여 OAuth2UserInfoDto 객체를 생성합니다.
	 * 새로운 소셜 로그인 제공자가 추가될 경우 이 메서드의 switch 문에 케이스를 추가하여 확장할 수 있습니다.
	 *
	 * @param registrationId 소셜 로그인 제공자의 ID (예: "google", "kakao", "naver").
	 * @param attributes OAuth2 제공자로부터 받은 원본 사용자 정보가 담긴 Map 객체.
	 * @return OAuth2UserInfoDto 해당 제공자의 사용자 정보로 초기화된 OAuth2UserInfoDto 객체.
	 * @throws IllegalArgumentException 지원하지 않는 소셜 로그인 제공자일 경우 발생합니다.
	 */
	public static OAuth2UserInfoDto of(String registrationId, Map<String, Object> attributes) {
		return switch (registrationId) {
			case "google" -> ofGoogle(attributes);
			case "kakao" -> ofKakao(attributes);
			case "naver" -> ofNaver(attributes);
			default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
		};
	}

}
