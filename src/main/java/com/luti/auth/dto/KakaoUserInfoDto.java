package com.luti.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Kakao OAuth2 사용자 정보 DTO
 * Kakao의 복잡한 중첩 구조와 동의항목 처리에 특화된 클래스입니다.
 *
 * @author
 */
@Slf4j
@Getter
@Builder
public class KakaoUserInfoDto implements OAuth2UserInfo {

	private final String providerId;      // 카카오 고유 ID
	private final String email;           // 카카오 이메일 (없으면 더미 이메일)
	private final String name;            // 카카오 이름 (없으면 닉네임 사용)
	private final String nickname;        // 카카오 닉네임 (필수)
	private final String birthday;        // 생년월일 (YYYYMMDD 형식)
	private final String gender;          // 성별 (남성/여성)
	private final String profileImageUrl; // 프로필 이미지 URL (필수)
	private final Map<String, Object> attributes; // 원본 응답 데이터

	/**
	 * Kakao OAuth2 응답으로부터 KakaoUserInfoDto 생성
	 * Kakao의 복잡한 중첩 구조를 파싱하고 동의하지 않은 항목을 처리합니다.
	 *
	 * @param attributes Kakao OAuth2에서 반환하는 사용자 정보 Map
	 * @return KakaoUserInfoDto 객체
	 */
	public static KakaoUserInfoDto from(Map<String, Object> attributes) {
		String kakaoId = String.valueOf(attributes.get("id"));

		// kakao_account 내부 정보 추출
		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

		// 필수 정보 (항상 제공됨)
		String nickname = (String) profile.get("nickname");
		String profileImageUrl = (String) profile.get("profile_image_url");

		// 선택 정보 (동의하지 않으면 null일 수 있음)
		String email = extractOptionalField(kakaoAccount, "email");
		String name = extractOptionalField(kakaoAccount, "name");
		String gender = extractOptionalField(kakaoAccount, "gender");
		String birthyear = extractOptionalField(kakaoAccount, "birthyear");
		String birthday = extractOptionalField(kakaoAccount, "birthday");

		// 이메일 처리: 없으면 더미 이메일 생성
		if (email == null || email.trim().isEmpty()) {
			email = "kakao_" + kakaoId + "@kakao.local";
			log.info("카카오 사용자 이메일 동의 없음 - 더미 이메일 생성: {}", email);
		}

		// 이름 처리: 없으면 닉네임 사용
		if (name == null || name.trim().isEmpty()) {
			name = nickname;
			log.debug("카카오 사용자 이름 동의 없음 - 닉네임 사용: {}", nickname);
		}

		// 생년월일 조합
		String fullBirthday = combineBirthday(birthyear, birthday);

		// 성별 변환
		String convertedGender = convertGender(gender);

		return KakaoUserInfoDto.builder()
				.providerId(kakaoId)
				.email(email)
				.name(name)
				.nickname(nickname)
				.birthday(fullBirthday)
				.gender(convertedGender)
				.profileImageUrl(profileImageUrl)
				.attributes(attributes)
				.build();
	}

	/**
	 * 선택 동의 항목 안전하게 추출
	 * 동의하지 않은 경우 null을 반환합니다.
	 */
	private static String extractOptionalField(Map<String, Object> kakaoAccount, String fieldName) {
		try {
			// 동의 여부 확인
			String agreementKey = fieldName + "_needs_agreement";
			Boolean needsAgreement = (Boolean) kakaoAccount.get(agreementKey);

			if (Boolean.TRUE.equals(needsAgreement)) {
				log.debug("카카오 {} 동의 필요 - 정보 제공되지 않음", fieldName);
				return null;
			}

			return (String) kakaoAccount.get(fieldName);
		} catch (Exception e) {
			log.warn("카카오 {} 필드 추출 실패: {}", fieldName, e.getMessage());
			return null;
		}
	}

	/**
	 * 출생연도와 생일을 조합하여 YYYYMMDD 형식으로 생성
	 */
	private static String combineBirthday(String birthyear, String birthday) {
		if (birthyear != null && birthday != null &&
			!birthyear.trim().isEmpty() && !birthday.trim().isEmpty()) {
			return birthyear + birthday; // "1990" + "1201" = "19901201"
		}
		return null;
	}

	/**
	 * 카카오 성별을 애플리케이션 형식으로 변환
	 */
	private static String convertGender(String kakaoGender) {
		if (kakaoGender == null || kakaoGender.trim().isEmpty()) {
			return null;
		}
		return switch (kakaoGender.toLowerCase()) {
			case "male" -> "남성";
			case "female" -> "여성";
			default -> {
				log.warn("알 수 없는 카카오 성별 값: {}", kakaoGender);
				yield null;
			}
		};
	}

	@Override
	public String getProviderId() {
		return providerId;
	}

	@Override
	public String getProvider() {
		return "kakao";
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNickname() {
		return nickname;
	}

	@Override
	public String getBirthday() {
		return birthday;
	}

	@Override
	public String getGender() {
		return gender;
	}

	@Override
	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public boolean isDummyEmail() {
		return email != null && email.contains("@kakao.local");
	}
}
