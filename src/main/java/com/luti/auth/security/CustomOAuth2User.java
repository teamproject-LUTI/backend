package com.luti.auth.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.luti.auth.entity.User;

import lombok.Getter;

/**
 * 설명: OAuth2 인증 시 Spring Security에서 사용되는 커스텀 OAuth2User 구현 클래스입니다.
 * 소셜 로그인 제공자로부터 받은 사용자 정보와 애플리케이션의 User 엔티티를 통합하여,
 * Spring Security 컨텍스트에서 사용자 정보를 일관되게 관리하고 접근할 수 있도록 합니다.
 *
 * @author
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

	private final User user; // 애플리케이션의 User 엔티티 객체

	private final Map<String, Object> attributes; // OAuth2 제공자로부터 받은 원본 사용자 속성 맵

	private final String attributeKey; // 사용자 속성을 식별하는 키 (예: Google의 "sub", Kakao의 "id")

	/**
	 * 설명: CustomOAuth2User 클래스의 생성자입니다.
	 * OAuth2 인증 후 얻은 User 엔티티와 원본 속성 맵을 초기화합니다.
	 *
	 * @param user 애플리케이션의 User 엔티티 객체.
	 * @param attributes OAuth2 제공자로부터 받은 원본 사용자 속성 맵.
	 * @param attributeKey 사용자 속성을 식별하는 키.
	 * @author
	 */
	public CustomOAuth2User(User user, Map<String, Object> attributes, String attributeKey) {
		this.user = user;
		this.attributes = attributes;
		this.attributeKey = attributeKey;
	}

	/**
	 * 설명: OAuth2 제공자로부터 받은 원본 사용자 속성 맵을 반환합니다.
	 *
	 * @return Map<String, Object> 원본 사용자 속성 맵.
	 * @author
	 */
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * 설명: Spring Security에서 사용될 사용자의 권한(역할) 목록을 반환합니다.
	 * User 엔티티의 userTypeId에 따라 적절한 ROLE을 부여합니다.
	 *
	 * @return Collection<? extends GrantedAuthority> 사용자의 권한 목록.
	 * @author
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// 사용자 타입에 따른 권한 설정
		String role = "ROLE_USER"; // 기본 역할은 USER로 설정
		if (user.getUserTypeId() != null && user.getUserTypeId().getUserTypeId() != null) {
			// userTypeId가 1이면 ROLE_USER, 2이면 ROLE_ADMIN을 부여
			switch (user.getUserTypeId().getUserTypeId().intValue()) {
				case 1 -> role = "ROLE_USER";
				case 2 -> role = "ROLE_ADMIN";
				default -> role = "ROLE_USER"; // 정의되지 않은 경우 기본값
			}
		}
		// 단일 권한을 가진 리스트를 반환합니다.
		return Collections.singletonList(new SimpleGrantedAuthority(role));
	}

	/**
	 * 설명: Spring Security의 OAuth2User 인터페이스에 따라 사용자의 고유 이름을 반환합니다.
	 * 여기서는 User 엔티티의 displayName(표시 이름)을 사용합니다.
	 *
	 * @return String 사용자의 고유 이름.
	 * @author
	 */
	@Override
	public String getName() {
		return user.getDisplayName();
	}

	/**
	 * 설명: 현재 인증된 사용자의 고유 ID를 반환합니다.
	 *
	 * @return Long 사용자의 고유 ID (userId).
	 * @author
	 */
	public Long getUserId() {
		return user.getUserId();
	}

	/**
	 * 설명: 현재 인증된 사용자의 이메일 주소를 반환합니다.
	 *
	 * @return String 사용자의 이메일 주소.
	 * @author
	 */
	public String getEmail() {
		return user.getEmail();
	}

	/**
	 * 설명: 현재 인증된 사용자의 사용자 타입 ID를 반환합니다.
	 * UserTypeId가 null인 경우 기본값인 1L (일반 사용자)을 반환합니다.
	 *
	 * @return Long 사용자의 타입 ID.
	 * @author
	 */
	public Long getUserTypeId() {
		return user.getUserTypeId() != null ? user.getUserTypeId().getUserTypeId() : 1L;
	}

	/**
	 * 설명: 현재 인증된 사용자가 소셜 로그인 사용자(일반 로그인이 아닌)인지 여부를 반환합니다.
	 *
	 * @return boolean 소셜 로그인 사용자이면 `true`, 그렇지 않으면 `false`.
	 * @author
	 */
	public boolean isSocialUser() {
		return user.isSocialUser();
	}

	/**
	 * 설명: 현재 인증된 소셜 로그인 사용자의 제공자명(예: "google", "kakao")을 반환합니다.
	 *
	 * @return String 소셜 제공자명.
	 * @author
	 */
	public String getSocialProvider() {
		return user.getSocialProvider();
	}

	/**
	 * 설명: 현재 인증된 사용자의 프로필 이미지 URL을 반환합니다.
	 *
	 * @return String 프로필 이미지 URL.
	 * @author
	 */
	public String getProfileImageUrl() {
		return user.getDisplayProfileImage();
	}

}
