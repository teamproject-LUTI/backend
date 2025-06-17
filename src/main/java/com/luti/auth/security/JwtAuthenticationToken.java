package com.luti.auth.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

/**
 * 설명: JWT(JSON Web Token) 기반 인증을 위한 커스텀 Authentication 토큰 클래스입니다.
 * Spring Security 6.x 환경에서 사용되며, JWT에서 추출한 사용자 정보와 권한을 캡슐화하여
 * SecurityContextHolder에 저장함으로써, 인증된 사용자의 정보를 애플리케이션 전반에서 접근할 수 있도록 합니다.
 *
 * @author
 */
@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	private final Long userId;

	private final String email;

	private final String name;

	private final String nickname;

	private final String profileImageUrl;

	private final Long userTypeId;

	private final String provider;

	/**
	 * 설명: JwtAuthenticationToken의 생성자입니다.
	 * JWT에서 추출된 사용자 정보와 부여된 권한들을 기반으로 인증 토큰 객체를 초기화합니다.
	 * 이 토큰은 생성 시점에 이미 인증된(`authenticated`) 상태로 설정됩니다.
	 *
	 * @param userId 사용자의 고유 ID.
	 * @param email 사용자의 이메일 주소.
	 * @param name 사용자의 이름.
	 * @param nickname 사용자의 닉네임.
	 * @param profileImageUrl 사용자의 프로필 이미지 URL.
	 * @param userTypeId 사용자의 타입 ID.
	 * @param authorities 사용자에게 부여된 권한(GrantedAuthority) 컬렉션.
	 * @author
	 */
	public JwtAuthenticationToken(Long userId, String email, String name,
			String nickname, String profileImageUrl,
			Long userTypeId, String provider,
			Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.userId = userId;
		this.email = email;
		this.name = name;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.userTypeId = userTypeId;
		this.provider = provider;
		setAuthenticated(true);
	}

	/**
	 * 설명: 현재 인증된 사용자의 닉네임을 반환합니다.
	 *
	 * @return String 사용자의 닉네임.
	 * @author
	 */
	public String getCurrentUserNickname() {
		return this.nickname;
	}

	/**
	 * 설명: 현재 인증된 사용자의 프로필 이미지 URL을 반환합니다.
	 *
	 * @return String 사용자의 프로필 이미지 URL.
	 * @author
	 */
	public String getCurrentUserProfileImage() {
		return this.profileImageUrl;
	}

	/**
	 * 설명: 현재 인증된 사용자가 소셜 로그인인 경우 해당 소셜 제공자명을 반환합니다.
	 *
	 * @return String 소셜 제공자명 (예: "google", "kakao").
	 * @author
	 */
	public String getCurrentUserProvider() {
		return this.provider;
	}

	/**
	 * 설명: 인증 자격 증명(credentials)을 반환합니다.
	 * JWT 기반 인증에서는 일반적으로 비밀번호와 같은 자격 증명이 토큰 내에 포함되지 않으므로 `null`을 반환합니다.
	 *
	 * @return Object 항상 `null`.
	 * @author
	 */
	@Override
	public Object getCredentials() {
		return null;
	}

	/**
	 * 설명: 인증된 주체(Principal)를 반환합니다.
	 * 여기서는 사용자의 고유 식별자인 `userId`를 Principal로 사용합니다.
	 *
	 * @return Object 사용자의 고유 ID (`userId`).
	 * @author
	 */
	@Override
	public Object getPrincipal() {
		return this.userId;
	}

	/**
	 * 설명: 현재 인증된 사용자의 고유 ID를 반환합니다.
	 * `getPrincipal()` 메서드의 반환 값과 동일합니다.
	 *
	 * @return Long 사용자의 고유 ID.
	 * @author
	 */
	public Long getCurrentUserId() {
		return this.userId;
	}

	/**
	 * 설명: 현재 인증된 사용자의 이메일 주소를 반환합니다.
	 *
	 * @return String 사용자의 이메일.
	 * @author
	 */
	public String getCurrentUserEmail() {
		return this.email;
	}

	/**
	 * 설명: 현재 인증된 사용자의 이름을 반환합니다.
	 *
	 * @return String 사용자의 이름.
	 * @author
	 */
	public String getCurrentUserName() {
		return this.name;
	}

	/**
	 * 설명: 현재 인증된 사용자의 타입 ID를 반환합니다.
	 *
	 * @return Long 사용자의 타입 ID.
	 * @author
	 */
	public Long getCurrentUserTypeId() {
		return this.userTypeId;
	}

	/**
	 * 설명: JwtAuthenticationToken 객체의 문자열 표현을 반환합니다.
	 * 로깅 및 디버깅 목적으로 사용됩니다. 민감한 정보는 포함하지 않습니다.
	 *
	 * @return String JwtAuthenticationToken의 문자열 표현.
	 * @author
	 */
	@Override
	public String toString() {
		return "JwtAuthenticationToken{" +
			   "userId=" + userId +
			   ", email='" + email + '\'' +
			   ", name='" + name + '\'' +
			   ", userTypeId=" + userTypeId +
			   ", authorities=" + getAuthorities() +
			   '}';
	}

}
