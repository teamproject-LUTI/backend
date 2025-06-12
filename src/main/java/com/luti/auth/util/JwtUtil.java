package com.luti.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 설명: JWT(JSON Web Token)의 생성, 파싱, 유효성 검증 및 토큰 정보 추출을 담당하는 유틸리티 클래스입니다.
 * 애플리케이션의 JWT 기반 인증 시스템에서 핵심적인 역할을 수행합니다.
 *
 * @author
 */
@Component // Spring 컴포넌트로 등록하여 의존성 주입이 가능하도록 합니다.
public class JwtUtil {

	private final SecretKey secretKey; // JWT 서명에 사용될 비밀 키 (HS256 알고리즘)

	private final long accessTokenExpiration; // Access Token의 만료 시간 (밀리초)

	private final long refreshTokenExpiration; // Refresh Token의 만료 시간 (밀리초)

	/**
	 * 설명: JwtUtil의 생성자입니다.
	 * application.properties 또는 application.yml에서 JWT 관련 설정을 주입받아 초기화합니다.
	 * 비밀 키는 SHA-256 알고리즘에 적합하도록 바이트 배열로 변환됩니다.
	 *
	 * @param secret JWT 서명에 사용될 비밀 문자열. 환경 변수 `jwt.secret`에서 주입됩니다.
	 * @param accessTokenExpiration Access Token의 유효 기간(밀리초). 환경 변수 `jwt.access-token-expiration`에서 주입됩니다.
	 * @param refreshTokenExpiration Refresh Token의 유효 기간(밀리초). 환경 변수 `jwt.refresh-token-expiration`에서 주입됩니다.
	 * @author
	 */
	public JwtUtil(
			@Value("${jwt.secret:mySecretKeyForJwtTokenGenerationThatIsLongEnoughForHS256Algorithm}") String secret,
			@Value("${jwt.access-token-expiration:1800000}") long accessTokenExpiration, // 기본값: 30분 (1800000ms)
			@Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration // 기본값: 7일 (604800000ms)
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 비밀 키 생성
		this.accessTokenExpiration = accessTokenExpiration;
		this.refreshTokenExpiration = refreshTokenExpiration;
	}

	/**
	 * 설명: 사용자 정보를 포함하는 Access Token을 생성합니다.
	 * 이 토큰은 짧은 만료 시간을 가지며, 인증된 요청에 사용됩니다.
	 *
	 * @param userId 사용자의 고유 ID.
	 * @param email 사용자의 이메일 주소.
	 * @param name 사용자의 이름.
	 * @param nickname 사용자의 닉네임.
	 * @param profileImageUrl 사용자의 프로필 이미지 URL.
	 * @param socialProvider 소셜 로그인 제공자 (예: "google", "kakao").
	 * @param userTypeId 사용자의 유형 ID.
	 * @return String 생성된 Access Token 문자열.
	 * @author
	 */
	public String generateAccessToken(Long userId, String email, String name,
			String nickname, String profileImageUrl,
			String socialProvider, Long userTypeId) {
		Date now = new Date(); // 현재 시간
		Date expiryDate = new Date(now.getTime() + accessTokenExpiration); // Access Token 만료 시간 계산

		return Jwts.builder()
				.subject(userId.toString()) // 토큰의 주체(subject)로 사용자 ID 설정
				.claim("email", email) // 클레임: 이메일
				.claim("name", name) // 클레임: 이름
				.claim("nickname", nickname) // 클레임: 닉네임
				.claim("profileImageUrl", profileImageUrl) // 클레임: 프로필 이미지 URL
				.claim("socialProvider", socialProvider) // 클레임: 소셜 제공자
				.claim("userTypeId", userTypeId) // 클레임: 사용자 타입 ID
				.claim("tokenType", "ACCESS") // 클레임: 토큰 타입 (ACCESS)
				.issuedAt(now) // 토큰 발급 시간
				.expiration(expiryDate) // 토큰 만료 시간
				.signWith(secretKey, Jwts.SIG.HS256) // 비밀 키와 HS256 알고리즘으로 서명
				.compact(); // JWT를 압축하여 문자열로 반환
	}

	/**
	 * 설명: 사용자 ID를 포함하는 Refresh Token을 생성합니다.
	 * 이 토큰은 긴 만료 시간을 가지며, Access Token 갱신에 사용됩니다.
	 *
	 * @param userId 사용자의 고유 ID.
	 * @return String 생성된 Refresh Token 문자열.
	 * @author
	 */
	public String generateRefreshToken(Long userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + refreshTokenExpiration); // Refresh Token 만료 시간 계산

		return Jwts.builder()
				.subject(userId.toString()) // 토큰의 주체(subject)로 사용자 ID 설정
				.claim("tokenType", "REFRESH") // 클레임: 토큰 타입 (REFRESH)
				.issuedAt(now) // 토큰 발급 시간
				.expiration(expiryDate) // 토큰 만료 시간
				.signWith(secretKey, Jwts.SIG.HS256) // 비밀 키와 HS256 알고리즘으로 서명
				.compact(); // JWT를 압축하여 문자열로 반환
	}

	/**
	 * 설명: 주어진 JWT 토큰에서 사용자 ID(subject)를 추출하여 반환합니다.
	 *
	 * @param token 사용자 ID를 추출할 JWT 토큰 문자열.
	 * @return Long 추출된 사용자 ID.
	 * @throws JwtException 토큰 파싱 또는 서명 검증 실패 시 발생.
	 * @author
	 */
	public Long getUserIdFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		return Long.parseLong(claims.getSubject());
	}

	/**
	 * 설명: 주어진 JWT 토큰에서 이메일 클레임을 추출하여 반환합니다.
	 *
	 * @param token 이메일을 추출할 JWT 토큰 문자열.
	 * @return String 추출된 이메일 주소.
	 * @throws JwtException 토큰 파싱 또는 서명 검증 실패 시 발생.
	 * @author
	 */
	public String getEmailFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		return claims.get("email", String.class);
	}

	/**
	 * 설명: 주어진 JWT 토큰의 유효성을 검증합니다.
	 * 서명 유효성, 만료 여부 등을 확인합니다.
	 *
	 * @param token 유효성을 검증할 JWT 토큰 문자열.
	 * @return boolean 토큰이 유효하면 `true`, 그렇지 않으면 `false`.
	 * @author
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * 설명: 주어진 JWT 토큰이 만료되었는지 여부를 확인합니다.
	 * (validateToken과는 별개로 만료 여부만 확인하고자 할 때 사용)
	 *
	 * @param token 만료 여부를 확인할 JWT 토큰 문자열.
	 * @return boolean 토큰이 만료되었으면 `true`, 만료되지 않았으면 `false`. 토큰 파싱 실패 시에도 `true` 반환.
	 * @author
	 */
	public boolean isTokenExpired(String token) {
		try {
			Date expiration = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getExpiration();
			return expiration.before(new Date());
		} catch (JwtException e) {
			return true;
		}
	}

	/**
	 * 설명: 주어진 JWT 토큰에서 `tokenType` 클레임(예: "ACCESS", "REFRESH")을 추출하여 반환합니다.
	 *
	 * @param token 토큰 타입을 추출할 JWT 토큰 문자열.
	 * @return String 토큰 타입 문자열.
	 * @throws JwtException 토큰 파싱 또는 서명 검증 실패 시 발생.
	 * @author
	 */
	public String getTokenType(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		return claims.get("tokenType", String.class);
	}

	/**
	 * 설명: 주어진 JWT 토큰에서 모든 클레임(Claims)을 추출하여 반환합니다.
	 * 토큰의 모든 페이로드 정보를 접근할 때 사용됩니다.
	 *
	 * @param token 클레임을 추출할 JWT 토큰 문자열.
	 * @return Claims 토큰의 모든 클레임을 담은 객체.
	 * @throws JwtException 토큰 파싱 또는 서명 검증 실패 시 발생.
	 * @author
	 */
	public Claims getClaimsFromToken(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload(); // 서명 검증 후 페이로드(클레임) 반환
	}

	/**
	 * 설명: 주어진 JWT 토큰에서 닉네임 클레임을 추출하여 반환합니다.
	 *
	 * @param token 닉네임을 추출할 JWT 토큰 문자열.
	 * @return String 추출된 닉네임.
	 * @throws JwtException 토큰 파싱 또는 서명 검증 실패 시 발생.
	 * @author
	 */
	public String getNicknameFromToken(String token) {
		Claims claims = getClaimsFromToken(token);
		return claims.get("nickname", String.class);
	}

	/**
	 * 설명: 주어진 JWT 토큰에서 프로필 이미지 URL 클레임을 추출하여 반환합니다.
	 *
	 * @param token 프로필 이미지 URL을 추출할 JWT 토큰 문자열.
	 * @return String 추출된 프로필 이미지 URL.
	 * @throws JwtException 토큰 파싱 또는 서명 검증 실패 시 발생.
	 * @author
	 */
	public String getProfileImageFromToken(String token) {
		Claims claims = getClaimsFromToken(token);
		return claims.get("profileImageUrl", String.class);
	}

	/**
	 * 설명: 주어진 JWT 토큰에서 소셜 제공자 클레임을 추출하여 반환합니다.
	 *
	 * @param token 소셜 제공자를 추출할 JWT 토큰 문자열.
	 * @return String 추출된 소셜 제공자명.
	 * @throws JwtException 토큰 파싱 또는 서명 검증 실패 시 발생.
	 * @author
	 */
	public String getSocialProviderFromToken(String token) {
		Claims claims = getClaimsFromToken(token);
		return claims.get("socialProvider", String.class);
	}

	/**
	 * 설명: Access Token의 설정된 만료 시간(밀리초)을 반환합니다.
	 *
	 * @return long Access Token의 만료 시간(밀리초).
	 * @author
	 */
	public long getAccessTokenExpiration() {
		return accessTokenExpiration;
	}

	/**
	 * 설명: Refresh Token의 설정된 만료 시간(밀리초)을 반환합니다.
	 *
	 * @return long Refresh Token의 만료 시간(밀리초).
	 * @author
	 */
	public long getRefreshTokenExpiration() {
		return refreshTokenExpiration;
	}

}
