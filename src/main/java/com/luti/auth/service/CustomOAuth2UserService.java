package com.luti.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.luti.auth.dto.OAuth2UserInfo;
import com.luti.auth.dto.OAuth2UserInfoFactory;
import com.luti.auth.entity.User;
import com.luti.auth.entity.UserType;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.repository.UserTypeRepository;
import com.luti.auth.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 소셜 로그인 시 사용자 정보를 로드하고 처리하는 서비스 클래스입니다.
 * 제공자별 DTO를 사용하여 각 소셜 플랫폼의 특성을 더 정확하게 처리합니다.
 * Google, Kakao 등 다중 소셜 제공자를 지원합니다.
 *
 * @author
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final UserTypeRepository userTypeRepository;

	/**
	 * OAuth2 인증 요청을 받아 사용자 정보를 로드하고 처리하는 핵심 메서드입니다.
	 * 팩토리 패턴을 사용하여 제공자별 DTO로 사용자 정보를 파싱합니다.
	 */
	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		log.info("OAuth2 사용자 정보 로딩 시작");

		try {
			// 1. OAuth2 제공자로부터 원본 사용자 정보 획득
			OAuth2User oauth2User = super.loadUser(userRequest);

			// 2. 제공자 정보 추출
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			String userNameAttributeName = userRequest.getClientRegistration()
					.getProviderDetails()
					.getUserInfoEndpoint()
					.getUserNameAttributeName();

			// 3. 팩토리를 통해 제공자별 DTO 생성
			OAuth2UserInfo userInfo;
			try {
				userInfo = OAuth2UserInfoFactory.createUserInfo(registrationId, oauth2User.getAttributes());
			} catch (IllegalArgumentException e) {
				log.error("지원하지 않는 OAuth2 제공자: {}", registrationId, e);
				throw new OAuth2AuthenticationException(
						createOAuth2Error("UNSUPPORTED_PROVIDER", e.getMessage())
				);
			}

			// 4. 사용자 정보를 데이터베이스에 저장하거나 업데이트
			User user = saveOrUpdateUser(userInfo);

			// 5. CustomOAuth2User 객체 생성하여 반환
			return new CustomOAuth2User(user, oauth2User.getAttributes(), userNameAttributeName);

		} catch (OAuth2AuthenticationException e) {
			// OAuth2AuthenticationException은 그대로 다시 던짐
			log.error("OAuth2 인증 예외 발생: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			// 기타 예외는 OAuth2AuthenticationException으로 래핑
			log.error("OAuth2 사용자 정보 로딩 중 예외 발생: {}", e.getMessage(), e);
			throw new OAuth2AuthenticationException(
					createOAuth2Error("OAUTH2_PROCESSING_ERROR", "사용자 정보 처리 중 오류가 발생했습니다.")
			);
		}
	}

	/**
	 * OAuth2 사용자 정보를 데이터베이스에 저장하거나 기존 사용자 정보를 업데이트합니다.
	 * 제공자별 DTO의 특성을 활용하여 더 정확한 처리를 수행합니다.
	 */
	@Transactional
	public User saveOrUpdateUser(OAuth2UserInfo userInfo) {
		try {
			// 1. 소셜 제공자와 소셜 ID로 기존 사용자 검색
			User user = userRepository.findBySocialProviderAndSocialId(
					userInfo.getProvider().toUpperCase(),
					userInfo.getProviderId()
			).orElse(null);

			if (user != null) {
				// 기존 소셜 사용자 정보 업데이트
				updateExistingSocialUser(user, userInfo);
				return userRepository.save(user);
			}

			// 2. 이메일로 기존 사용자 검색 (더미 이메일은 제외)
			if (!userInfo.isDummyEmail()) {
				user = userRepository.findByEmail(userInfo.getEmail()).orElse(null);

				if (user != null) {
					if (user.isSocialUser()) {
						// 다른 소셜 제공자로 로그인 시도
						user.setSocialProvider(userInfo.getProvider(), userInfo.getProviderId());
						updateExistingSocialUser(user, userInfo);
					} else {
						// 일반 계정과 이메일 중복
						log.warn("이메일 중복 - 일반 계정과 소셜 계정 충돌: {}", userInfo.getEmail());
						throw new OAuth2AuthenticationException(
								createOAuth2Error("EMAIL_ALREADY_EXISTS", "이미 해당 이메일로 가입된 계정이 있습니다.")
						);
					}
					return userRepository.save(user);
				}
			}

			// 3. 새로운 소셜 사용자 생성
			return createNewSocialUser(userInfo);

		} catch (OAuth2AuthenticationException e) {
			// OAuth2AuthenticationException은 그대로 다시 던짐
			throw e;
		} catch (Exception e) {
			log.error("사용자 저장/업데이트 중 예외 발생: {}", e.getMessage(), e);
			throw new OAuth2AuthenticationException(
					createOAuth2Error("USER_SAVE_ERROR", "사용자 정보 저장 중 오류가 발생했습니다.")
			);
		}
	}

	/**
	 * 기존 소셜 사용자 정보 업데이트
	 * 각 제공자의 특성에 맞게 정보를 업데이트합니다.
	 */
	private void updateExistingSocialUser(User user, OAuth2UserInfo userInfo) {
		// 기본 정보 업데이트
		if (userInfo.getName() != null && !userInfo.getName().trim().isEmpty()) {
			user.setName(userInfo.getName());
		}

		if (userInfo.getNickname() != null && !userInfo.getNickname().trim().isEmpty()) {
			// 기존 닉네임이 없거나 이름과 같았던 경우에만 업데이트
			if (user.getNickname() == null || user.getNickname().equals(user.getName())) {
				user.setNickname(userInfo.getNickname());
			}
		}

		// 프로필 이미지 업데이트
		if (userInfo.getProfileImageUrl() != null && !userInfo.getProfileImageUrl().trim().isEmpty()) {
			user.setProfileLogicalPath(userInfo.getProfileImageUrl());
		}

		// 제공자별 추가 정보 업데이트
		updateProviderSpecificInfo(user, userInfo);
	}

	/**
	 * 제공자별 특화 정보 업데이트
	 */
	private void updateProviderSpecificInfo(User user, OAuth2UserInfo userInfo) {
		String provider = userInfo.getProvider();

		if ("kakao".equals(provider)) {
			// 카카오: 생년월일 업데이트 (기존에 없던 경우)
			if (userInfo.getBirthday() != null &&
				(user.getBirthday() == null || user.getBirthday().trim().isEmpty())) {
				user.setBirthday(userInfo.getBirthday());
				log.debug("카카오 사용자 생년월일 업데이트: {}", userInfo.getBirthday());
			}

			// 카카오: 성별 업데이트 (기존에 없던 경우)
			if (userInfo.getGender() != null &&
				(user.getGender() == null || user.getGender().trim().isEmpty())) {
				user.setGender(userInfo.getGender());
				log.debug("카카오 사용자 성별 업데이트: {}", userInfo.getGender());
			}

			// 카카오: 실제 이메일로 업데이트 (기존이 더미 이메일이고 새로 실제 이메일을 받은 경우)
			if (!userInfo.isDummyEmail() && isDummyEmail(user.getEmail())) {
				if (!userRepository.existsByEmail(userInfo.getEmail())) {
					user.setEmail(userInfo.getEmail());
				}
			}
		}
		// 구글의 경우 기본 정보만 업데이트 (추가 정보 없음)
	}

	/**
	 * 새로운 소셜 사용자 생성
	 */
	private User createNewSocialUser(OAuth2UserInfo userInfo) {
		try {
			UserType defaultUserType = userTypeRepository.findDefaultUserType()
					.orElseThrow(() -> new RuntimeException("기본 사용자 타입을 찾을 수 없습니다."));

			User user = User.createSocialUser(
					userInfo.getEmail(),
					userInfo.getName(),
					userInfo.getNickname(),
					userInfo.getBirthday(),  // 카카오의 경우 생년월일 포함, 구글의 경우 null
					userInfo.getGender(),    // 카카오의 경우 성별 포함, 구글의 경우 null
					userInfo.getProfileImageUrl(),
					defaultUserType
			);

			user.setSocialProvider(userInfo.getProvider(), userInfo.getProviderId());

			User savedUser = userRepository.save(user);

			return savedUser;

		} catch (Exception e) {
			log.error("새로운 소셜 사용자 생성 중 예외 발생: {}", e.getMessage(), e);
			throw new OAuth2AuthenticationException(
					createOAuth2Error("USER_CREATION_ERROR", "새 사용자 생성 중 오류가 발생했습니다.")
			);
		}
	}

	/**
	 * 더미 이메일인지 확인
	 */
	private boolean isDummyEmail(String email) {
		return email != null && (email.contains("@kakao.local") || email.contains("@google.local"));
	}

	/**
	 * OAuth2Error 객체 생성 유틸리티 메서드
	 */
	private OAuth2Error createOAuth2Error(String errorCode, String description) {
		return new OAuth2Error(errorCode, description, null);
	}
}
