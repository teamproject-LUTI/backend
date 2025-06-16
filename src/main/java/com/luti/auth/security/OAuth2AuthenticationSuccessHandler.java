package com.luti.auth.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.luti.auth.entity.RefreshToken;
import com.luti.auth.entity.User;
import com.luti.auth.repository.RefreshTokenRepository;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.util.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtUtil jwtUtil;

	private final RefreshTokenRepository refreshTokenRepository;

	private final UserRepository userRepository;

	@Value("${app.frontend.url:http://localhost:3000}")
	private String frontendUrl;

	// 성공 시 메인 페이지로 직접 리다이렉트
	@Value("${app.frontend.oauth2.success-redirect-path:/main}")
	private String successRedirectPath;

	@Override
	@Transactional
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		log.info("OAuth2 인증 성공 처리 시작");

		try {
			CustomOAuth2User oauth2User = (CustomOAuth2User)authentication.getPrincipal();
			User user = oauth2User.getUser();

			log.info("OAuth2 로그인 성공 - 사용자 ID: {}, 이메일: {}", user.getUserId(), user.getEmail());

			// *** 탈퇴한 사용자 확인 (새로 추가) ***
			if ("Y".equals(user.getWithdrawYn())) {
				log.info("탈퇴한 사용자 OAuth2 로그인 시도 감지 - 사용자 ID: {}", user.getUserId());

				// 복구 페이지로 리다이렉트
				String restoreUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/restore")
						.queryParam("type", "oauth2")
						.queryParam("message", "탈퇴한 계정입니다. 복구하시겠습니까?")
						.build().toUriString();

				log.info("탈퇴한 사용자 복구 페이지로 리다이렉트: {}", restoreUrl);
				getRedirectStrategy().sendRedirect(request, response, restoreUrl);
				return;
			}

			// 정상 사용자인 경우 기존 로직 수행
			// JWT 토큰 생성
			String accessToken = jwtUtil.generateAccessToken(
					user.getUserId(),
					user.getEmail(),
					user.getDisplayName(),
					user.getNickname(),
					user.getDisplayProfileImage(),
					user.getUserTypeId() != null ? user.getUserTypeId().getUserTypeId() : 1L,
					user.getProvider()
			);

			String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

			// Refresh Token을 DB에 저장
			saveRefreshToken(user, refreshToken, request);

			// Access Token도 HttpOnly 쿠키로 설정
			setAccessTokenCookie(response, accessToken);

			// Refresh Token을 HttpOnly 쿠키로 설정
			setRefreshTokenCookie(response, refreshToken);

			// OAuth2 인증 완료 후 세션 무효화 및 JSESSIONID 쿠키 삭제
			invalidateSessionAndClearCookie(request, response);

			// 메인 페이지로 직접 리다이렉트
			String redirectUrl = frontendUrl + successRedirectPath;

			log.info("OAuth2 로그인 성공 - 메인 페이지로 직접 리다이렉트: {}", redirectUrl);
			getRedirectStrategy().sendRedirect(request, response, redirectUrl);

		} catch (Exception e) {
			log.error("OAuth2 인증 성공 처리 중 오류 발생", e);

			String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/error")
					.queryParam("message", "로그인 처리 중 오류가 발생했습니다.")
					.build().toUriString();

			getRedirectStrategy().sendRedirect(request, response, errorUrl);
		}
	}

	/**
	 * OAuth2 인증 완료 후 세션 무효화 및 JSESSIONID 쿠키 삭제
	 * JWT 토큰 기반 인증으로 전환하므로 OAuth2 과정에서 생성된 세션을 정리
	 */
	private void invalidateSessionAndClearCookie(HttpServletRequest request, HttpServletResponse response) {
		try {
			// 현재 세션이 존재하는지 확인 (새 세션 생성하지 않음)
			HttpSession session = request.getSession(false);
			if (session != null) {
				String sessionId = session.getId();
				session.invalidate(); // 세션 무효화
				log.debug("OAuth2 인증 완료 후 세션 무효화 완료 - Session ID: {}", sessionId);
			}

			// JSESSIONID 쿠키 삭제
			clearJSessionIdCookie(response);
			log.debug("JSESSIONID 쿠키 삭제 완료");

		} catch (Exception e) {
			log.warn("세션 무효화 중 오류 발생 (JWT 인증에는 영향 없음): {}", e.getMessage());
		}
	}

	/**
	 * JSESSIONID 쿠키를 삭제
	 */
	private void clearJSessionIdCookie(HttpServletResponse response) {
		Cookie jSessionCookie = new Cookie("JSESSIONID", "");
		jSessionCookie.setHttpOnly(true);
		jSessionCookie.setSecure(true);
		jSessionCookie.setPath("/");
		jSessionCookie.setMaxAge(0); // 즉시 만료
		response.addCookie(jSessionCookie);
	}

	/**
	 * Access Token을 HttpOnly 쿠키로 설정
	 */
	private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
		Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
		accessTokenCookie.setHttpOnly(true);        // JavaScript 접근 차단
		accessTokenCookie.setSecure(true);          // HTTPS에서만 전송
		accessTokenCookie.setPath("/");
		accessTokenCookie.setMaxAge((int)(jwtUtil.getAccessTokenExpiration() / 1000)); // 30분

		response.addCookie(accessTokenCookie);
		log.debug("Access Token 쿠키 설정 완료");
	}

	/**
	 * Refresh Token을 HttpOnly 쿠키로 설정
	 */
	private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge((int)(jwtUtil.getRefreshTokenExpiration() / 1000)); // 7일

		response.addCookie(refreshTokenCookie);
		log.debug("Refresh Token 쿠키 설정 완료");
	}

	/**
	 * Refresh Token을 DB에 저장
	 */
	@Transactional
	protected void saveRefreshToken(User user, String refreshToken, HttpServletRequest request) {
		try {
			refreshTokenRepository.revokeAllByUser(user);

			String deviceInfo = extractDeviceInfo(request);
			LocalDateTime expiresAt = LocalDateTime.now()
					.plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000);

			RefreshToken refreshTokenEntity = RefreshToken.builder()
					.user(user)
					.tokenValue(refreshToken)
					.expiresAt(expiresAt)
					.deviceInfo(deviceInfo)
					.build();

			refreshTokenRepository.save(refreshTokenEntity);
			log.info("Refresh Token 저장 완료 - 사용자 ID: {}", user.getUserId());

		} catch (Exception e) {
			log.error("Refresh Token 저장 중 오류 발생", e);
			throw new RuntimeException("토큰 저장에 실패했습니다.", e);
		}
	}

	private String extractDeviceInfo(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		String clientIp = getClientIpAddress(request);

		return String.format("IP: %s, UserAgent: %s",
				clientIp != null ? clientIp : "unknown",
				userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) : "unknown");
	}

	private String getClientIpAddress(HttpServletRequest request) {
		String[] headerNames = {
				"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
				"WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
				"HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
				"HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
		};

		for (String header : headerNames) {
			String ip = request.getHeader(header);
			if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
				return ip.split(",")[0].trim();
			}
		}

		return request.getRemoteAddr();
	}

}
