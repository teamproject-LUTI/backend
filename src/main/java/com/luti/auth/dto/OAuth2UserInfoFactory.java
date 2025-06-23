package com.luti.auth.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * OAuth2 사용자 정보 DTO 생성을 담당하는 팩토리 클래스
 * 소셜 로그인 제공자에 따라 적절한 DTO를 생성합니다.
 *
 * @author
 */
@Slf4j
public class OAuth2UserInfoFactory {

	/**
	 * 소셜 로그인 제공자에 따라 적절한 OAuth2UserInfo 구현체를 생성합니다.
	 *
	 * @param registrationId 소셜 로그인 제공자 ID (예: "google", "kakao", "naver")
	 * @param attributes OAuth2 제공자로부터 받은 원본 사용자 정보 Map
	 * @return OAuth2UserInfo 해당 제공자의 사용자 정보 DTO
	 * @throws IllegalArgumentException 지원하지 않는 소셜 로그인 제공자일 경우
	 */
	public static OAuth2UserInfo createUserInfo(String registrationId, Map<String, Object> attributes) {
		log.info("OAuth2UserInfo 생성 - 제공자: {}", registrationId);

		if (registrationId == null) {
			throw new IllegalArgumentException("registrationId가 null입니다.");
		}

		if (attributes == null || attributes.isEmpty()) {
			throw new IllegalArgumentException("사용자 정보 attributes가 비어있습니다.");
		}

		return switch (registrationId.toLowerCase()) {
			case "google" -> {
				log.debug("Google 사용자 정보 DTO 생성");
				yield GoogleUserInfoDto.from(attributes);
			}
			case "kakao" -> {
				log.debug("Kakao 사용자 정보 DTO 생성");
				yield KakaoUserInfoDto.from(attributes);
			}
			case "naver" -> {
				log.debug("Naver 사용자 정보 DTO 생성 (미구현)");
				// TODO: NaverUserInfoDto 구현 시 추가
				throw new IllegalArgumentException("Naver 로그인은 아직 구현되지 않았습니다.");
			}
			default -> {
				log.error("지원하지 않는 OAuth2 제공자: {}", registrationId);
				throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
			}
		};
	}

	/**
	 * 지원되는 OAuth2 제공자인지 확인합니다.
	 *
	 * @param registrationId 확인할 제공자 ID
	 * @return 지원되는 제공자이면 true, 아니면 false
	 */
	public static boolean isSupported(String registrationId) {
		if (registrationId == null) {
			return false;
		}

		return switch (registrationId.toLowerCase()) {
			case "google", "kakao" -> true;
			case "naver" -> false;
			default -> false;
		};
	}

	/**
	 * 지원되는 모든 OAuth2 제공자 목록을 반환합니다.
	 *
	 * @return 지원되는 제공자 배열
	 */
	public static String[] getSupportedProviders() {
		return new String[]{"google", "kakao"};
	}
}
