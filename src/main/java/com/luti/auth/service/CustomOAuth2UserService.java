package com.luti.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.luti.auth.dto.OAuth2UserInfoDto;
import com.luti.auth.entity.User;
import com.luti.auth.entity.UserType;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.repository.UserTypeRepository;
import com.luti.auth.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 설명: OAuth2 소셜 로그인 시 사용자 정보를 로드하고 처리하는 서비스 클래스입니다.
 * Spring Security OAuth2의 `DefaultOAuth2UserService`를 확장하여,
 * OAuth2 제공자로부터 받은 사용자 정보를 애플리케이션의 `User` 엔티티와 연동하고
 * 데이터베이스에 저장하거나 업데이트하는 로직을 수행합니다.
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
	 * 설명: OAuth2 인증 요청을 받아 사용자 정보를 로드하고 처리하는 핵심 메서드입니다.
	 * `DefaultOAuth2UserService`의 `loadUser`를 호출하여 OAuth2 제공자로부터 원본 사용자 정보를 가져온 후,
	 * 이를 애플리케이션의 형식에 맞춰 파싱하고, 데이터베이스에 사용자 정보를 저장 또는 업데이트합니다.
	 * 최종적으로 Spring Security 컨텍스트에 저장될 `CustomOAuth2User` 객체를 반환합니다.
	 *
	 * @param userRequest OAuth2 사용자 정보 요청 객체. 클라이언트 등록 정보 및 액세스 토큰을 포함합니다.
	 * @return OAuth2User Spring Security 컨텍스트에 저장될 `CustomOAuth2User` 객체.
	 * @throws OAuth2AuthenticationException OAuth2 인증 중 오류가 발생할 경우.
	 * @author
	 */
	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		log.info("OAuth2 사용자 정보 로딩 시작");

		// 1. 부모 클래스의 loadUser를 호출하여 OAuth2 제공자로부터 원본 사용자 정보(OAuth2User)를 획득합니다.
		OAuth2User oauth2User = super.loadUser(userRequest);

		// 2. OAuth2 제공자(RegistrationId)와 사용자명 속성(UserNameAttributeName) 정보를 추출합니다.
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration()
				.getProviderDetails()
				.getUserInfoEndpoint()
				.getUserNameAttributeName();

		log.info("OAuth2 제공자: {}, 사용자명 속성: {}", registrationId, userNameAttributeName);

		// 3. 획득한 원본 사용자 정보를 애플리케이션의 공통 DTO(OAuth2UserInfoDto) 형식으로 파싱합니다.
		OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.of(registrationId, oauth2User.getAttributes());

		log.info("OAuth2 사용자 정보 - 이메일: {}, 이름: {}, 제공자: {}",
				userInfo.getEmail(), userInfo.getName(), userInfo.getProvider());

		// 4. 파싱된 사용자 정보를 데이터베이스에 저장하거나 업데이트합니다.
		User user = saveOrUpdateUser(userInfo);

		// 5. Spring Security 컨텍스트에 저장될 CustomOAuth2User 객체를 생성하여 반환합니다.
		return new CustomOAuth2User(user, oauth2User.getAttributes(), userNameAttributeName);
	}

	/**
	 * 설명: OAuth2 사용자 정보를 데이터베이스에 저장하거나 기존 사용자 정보를 업데이트합니다.
	 *
	 * @param userInfo 파싱된 OAuth2 사용자 정보 DTO.
	 * @return User 저장 또는 업데이트된 User 엔티티.
	 * @throws OAuth2AuthenticationException 이메일 중복 등 인증 관련 오류가 발생할 경우.
	 * @author
	 */
	@Transactional
	public User saveOrUpdateUser(OAuth2UserInfoDto userInfo) {
		// 소셜 제공자와 소셜 ID로 기존 사용자 검색
		User user = userRepository.findBySocialProviderAndSocialId(
				userInfo.getProvider().toUpperCase(), // "google" -> "GOOGLE"
				userInfo.getProviderId()
		).orElse(null);

		if (user != null) {
			// 기존 소셜 사용자 정보 업데이트
			user.updateSocialInfo(userInfo.getName(), userInfo.getProfileImageUrl());
			return userRepository.save(user);
		}

		// 이메일로 기존 사용자 검색
		user = userRepository.findByEmail(userInfo.getEmail()).orElse(null);

		if (user != null) {
			if (user.isSocialUser()) {
				// 다른 소셜 제공자로 로그인 시도 - 새 제공자로 업데이트
				user.setSocialProvider(userInfo.getProvider(), userInfo.getProviderId());
				user.updateSocialInfo(userInfo.getName(), userInfo.getProfileImageUrl());
			} else {
				// 일반 계정과 이메일 중복
				throw new OAuth2AuthenticationException("이미 해당 이메일로 가입된 계정이 있습니다.");
			}
			return userRepository.save(user);
		}

		// 새로운 소셜 사용자 생성
		UserType defaultUserType = userTypeRepository.findDefaultUserType()
				.orElseThrow(() -> new OAuth2AuthenticationException("기본 사용자 타입을 찾을 수 없습니다."));

		user = User.createSocialUser(
				userInfo.getEmail(),
				userInfo.getName(),
				userInfo.getProfileImageUrl(),
				defaultUserType
		);

		user.setSocialProvider(userInfo.getProvider(), userInfo.getProviderId());

		return userRepository.save(user);
	}

}
