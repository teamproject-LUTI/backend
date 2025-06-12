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
		// 1. 소셜 제공자(provider)와 소셜 ID(providerId)를 이용하여 데이터베이스에서 기존 사용자를 검색합니다.
		// 이는 동일한 소셜 계정으로 재로그인하는 경우를 처리합니다.
		User user = userRepository.findBySocialProviderAndSocialId(
				userInfo.getProvider(),
				userInfo.getProviderId()
		).orElse(null);

		if (user != null) {
			// 1-1. 기존 소셜 사용자가 존재하는 경우, 프로필 정보(이름, 프로필 이미지)를 업데이트합니다.
			log.info("기존 소셜 사용자 정보 업데이트: {}", userInfo.getEmail());
			user.updateSocialInfo(userInfo.getName(), userInfo.getProfileImageUrl());
			return userRepository.save(user); // 변경된 사용자 정보 저장
		}

		// 2. 소셜 ID로 사용자를 찾지 못했다면, 이메일 주소를 이용하여 기존 사용자를 검색합니다.
		user = userRepository.findByEmail(userInfo.getEmail()).orElse(null);

		if (user != null) {
			// 2-1. 이메일로 기존 사용자를 찾았을 경우
			if (user.isSocialUser()) {
				// 2-1-1. 이미 소셜 로그인 사용자이고, 다른 소셜 제공자로 로그인 시도하는 경우
				// 기존 소셜 계정에 새로운 소셜 제공자 정보를 연동합니다.
				log.info("기존 소셜 사용자에 다른 제공자 연동: {}", userInfo.getEmail());
				user.setSocialProvider(userInfo.getProvider(), userInfo.getProviderId());
				user.updateSocialInfo(userInfo.getName(), userInfo.getProfileImageUrl());
			} else {
				// 2-1-2. 이메일이 일반 로그인(로컬 계정) 사용자와 중복되는 경우
				log.warn("일반 로그인 사용자와 동일한 이메일로 소셜 로그인 시도: {}", userInfo.getEmail());
				throw new OAuth2AuthenticationException(
						"이미 해당 이메일로 가입된 계정이 있습니다. 기존 계정으로 로그인해주세요."
				);
			}
			return userRepository.save(user);
		}

		// 3. 위의 모든 조건에 해당하지 않는 경우, 새로운 소셜 사용자를 생성합니다.
		log.info("새로운 소셜 사용자 생성: {}", userInfo.getEmail());

		// 3-1. 시스템에 정의된 기본 사용자 타입(ROLE)을 조회합니다.
		UserType defaultUserType = userTypeRepository.findDefaultUserType()
				.orElseThrow(() -> new OAuth2AuthenticationException(
						"기본 사용자 타입을 찾을 수 없습니다." // 이 메시지는 OAuth2AuthenticationFailureHandler에서 처리됩니다.
				));

		// 3-2. OAuth2UserInfoDto의 정보와 기본 사용자 타입을 사용하여 새로운 User 엔티티를 생성합니다.
		user = User.createSocialUser(
				userInfo.getEmail(),
				userInfo.getName(),
				userInfo.getProfileImageUrl(),
				defaultUserType
		);

		// 3-3. 생성된 사용자 엔티티에 소셜 제공자 정보를 설정합니다.
		user.setSocialProvider(userInfo.getProvider(), userInfo.getProviderId());

		return userRepository.save(user);
	}

}
