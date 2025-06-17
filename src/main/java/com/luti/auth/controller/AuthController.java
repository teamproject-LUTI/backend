package com.luti.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.auth.service.AuthService;
import com.luti.auth.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 설명: 인증 관련 API 요청을 처리하는 컨트롤러 클래스.
 * JWT 기반의 인증 시스템을 사용하여 사용자 로그인, 토큰 갱신, 로그아웃, 사용자 정보 조회 등의 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	private final JwtUtil jwtUtil;

	/**
	 * 설명: Refresh Token을 사용하여 Access Token과 Refresh Token을 갱신합니다.
	 * 클라이언트로부터 전달받은 Refresh Token을 검증하고, 유효한 경우 새로운 Access Token과 Refresh Token을 발급하여 HTTP Only 쿠키로 설정합니다.
	 *
	 * @param request HttpServletRequest 객체, Refresh Token을 쿠키에서 추출하는 데 사용됩니다.
	 * @param response HttpServletResponse 객체, 새로운 Access Token과 Refresh Token을 쿠키로 설정하는 데 사용됩니다.
	 * @return ResponseEntity<Map < String, Object>> 성공 시 토큰 갱신 성공 메시지를, 실패 시 에러 메시지를 포함하는 응답을 반환합니다.
	 * @throws Exception 토큰 갱신 과정에서 발생할 수 있는 일반적인 예외입니다.
	 * @author
	 */
	@PostMapping("/refresh")
	public ResponseEntity<Map<String, Object>> refreshToken(
			HttpServletRequest request,
			HttpServletResponse response) {

		log.info("토큰 갱신 요청");

		try {
			String refreshToken = extractRefreshTokenFromCookie(request);

			if (refreshToken == null) {
				log.warn("Refresh Token이 없음");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증 토큰이 없습니다."));
			}

			AuthService.TokenRefreshResult result = authService.refreshAccessToken(refreshToken);

			if (!result.isSuccess()) {
				log.warn("토큰 갱신 실패: {}", result.getErrorMessage());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse(result.getErrorMessage()));
			}

			//  새로운 토큰들을 쿠키로 설정
			setAccessTokenCookie(response, result.getAccessToken());
			setRefreshTokenCookie(response, result.getRefreshToken());

			//  응답에서는 토큰을 직접 반환하지 않음
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("message", "토큰이 갱신되었습니다.");

			log.info("토큰 갱신 성공");
			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			log.error("토큰 갱신 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}

	/**
	 * 설명: 현재 사용자를 로그아웃 처리하고 Access Token 및 Refresh Token 쿠키를 삭제합니다.
	 * SecurityContextHolder에서 현재 인증 정보를 가져와 사용자 ID를 추출하고, AuthService를 통해 로그아웃 비즈니스 로직을 수행합니다.
	 *
	 * @param request HttpServletRequest 객체, Refresh Token을 쿠키에서 추출하는 데 사용됩니다.
	 * @param response HttpServletResponse 객체, 쿠키를 삭제하는 데 사용됩니다.
	 * @return ResponseEntity<Map < String, Object>> 성공 시 로그아웃 성공 메시지를, 실패 시 에러 메시지를 포함하는 응답을 반환합니다.
	 * @throws Exception 로그아웃 처리 중 발생할 수 있는 일반적인 예외입니다.
	 * @author
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(
			HttpServletRequest request,
			HttpServletResponse response) {

		log.info("로그아웃 요청");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication instanceof JwtAuthenticationToken jwtAuth) {
				Long userId = jwtAuth.getCurrentUserId();
				String refreshToken = extractRefreshTokenFromCookie(request);

				authService.logout(userId, refreshToken);
				log.info("로그아웃 성공 - 사용자 ID: {}", userId);
			}

			//  쿠키 삭제
			clearAccessTokenCookie(response);
			clearRefreshTokenCookie(response);
			clearJSessionIdCookie(response);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("message", "로그아웃되었습니다.");

			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			log.error("로그아웃 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("로그아웃 처리 중 오류가 발생했습니다."));
		}
	}

	/**
	 * 설명: 현재 사용자를 모든 디바이스에서 로그아웃 처리합니다.
	 * SecurityContextHolder에서 현재 인증 정보를 가져와 사용자 ID를 추출하고, AuthService를 통해 모든 디바이스에서의 로그아웃 비즈니스 로직을 수행합니다.
	 * 현재 디바이스의 쿠키(Access Token, Refresh Token)도 삭제합니다.
	 *
	 * @param request HttpServletRequest 객체, 요청 정보를 포함합니다.
	 * @param response HttpServletResponse 객체, 현재 디바이스의 쿠키를 삭제하는 데 사용됩니다.
	 * @return ResponseEntity<Map < String, Object>> 성공 시 모든 디바이스 로그아웃 성공 메시지를, 실패 시 에러 메시지를 포함하는 응답을 반환합니다.
	 * @throws Exception 모든 디바이스 로그아웃 처리 중 발생할 수 있는 일반적인 예외입니다.
	 * @author
	 */
	@PostMapping("/logout-all")
	public ResponseEntity<Map<String, Object>> logoutFromAllDevices(
			HttpServletRequest request,
			HttpServletResponse response) {

		log.info("모든 디바이스 로그아웃 요청");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();
			authService.logoutFromAllDevices(userId);

			//  현재 디바이스의 쿠키도 삭제
			clearAccessTokenCookie(response);
			clearRefreshTokenCookie(response);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("message", "모든 디바이스에서 로그아웃되었습니다.");

			log.info("모든 디바이스 로그아웃 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			log.error("모든 디바이스 로그아웃 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("로그아웃 처리 중 오류가 발생했습니다."));
		}
	}

	/**
	 * 설명: 현재 인증된 사용자의 상세 정보를 조회하여 반환합니다.
	 * SecurityContextHolder에서 현재 인증 정보를 가져와 사용자 ID, 이메일, 이름, 닉네임, 프로필 이미지 URL, 소셜 프로바이더, 사용자 유형 ID, 권한 등의 정보를 추출합니다.
	 *
	 * @return ResponseEntity<Map < String, Object>> 성공 시 사용자 정보를 포함하는 응답을, 인증 실패 시 에러 메시지를 포함하는 응답을 반환합니다.
	 * @throws Exception 사용자 정보 조회 중 발생할 수 있는 일반적인 예외입니다.
	 * @author
	 */
	@GetMapping("/me")
	public ResponseEntity<Map<String, Object>> getCurrentUser() {
		log.info("현재 사용자 정보 조회 요청");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("userId", jwtAuth.getCurrentUserId());
			userInfo.put("email", jwtAuth.getCurrentUserEmail());
			userInfo.put("name", jwtAuth.getCurrentUserName());
			userInfo.put("nickname", jwtAuth.getCurrentUserNickname());
			userInfo.put("profileImageUrl", jwtAuth.getCurrentUserProfileImage());
			userInfo.put("provider", jwtAuth.getCurrentUserProvider());
			userInfo.put("userTypeId", jwtAuth.getCurrentUserTypeId());
			userInfo.put("authorities", jwtAuth.getAuthorities());

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("user", userInfo);

			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			log.error("사용자 정보 조회 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("사용자 정보 조회에 실패했습니다."));
		}
	}

	/**
	 * 설명: 현재 요청에 포함된 Access Token의 유효성을 검증합니다.
	 * SecurityContextHolder에서 인증 정보를 확인하여 토큰의 유효 여부와 사용자 ID를 반환합니다.
	 *
	 * @return ResponseEntity<Map < String, Object>> 유효한 토큰인 경우 성공 메시지와 사용자 ID를, 유효하지 않은 경우 에러 메시지를 반환합니다.
	 * @author
	 */
	@GetMapping("/validate")
	public ResponseEntity<Map<String, Object>> validateToken() {
		log.debug("토큰 유효성 검증 요청");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof JwtAuthenticationToken jwtAuth) {
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("valid", true);
			responseBody.put("userId", jwtAuth.getCurrentUserId());

			return ResponseEntity.ok(responseBody);
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(createErrorResponse("유효하지 않은 토큰입니다."));
	}

	// ===== 쿠키 관련 유틸리티 메서드들 =====

	/**
	 * 설명: HttpServletRequest에서 "refreshToken" 이름의 쿠키 값을 추출합니다.
	 *
	 * @param request HttpServletRequest 객체, 쿠키 정보를 포함합니다.
	 * @return String Refresh Token 문자열 또는 쿠키가 없는 경우 null을 반환합니다.
	 * @author
	 */
	private String extractRefreshTokenFromCookie(HttpServletRequest request) {
		return extractTokenFromCookie(request, "refreshToken");
	}

	/**
	 * 설명: HttpServletRequest에서 지정된 이름의 쿠키 값을 추출합니다.
	 *
	 * @param request HttpServletRequest 객체, 쿠키 정보를 포함합니다.
	 * @param cookieName 추출할 쿠키의 이름입니다.
	 * @return String 지정된 이름의 쿠키 값 또는 쿠키가 없는 경우 null을 반환합니다.
	 * @author
	 */
	private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookieName.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 설명: Access Token을 HTTP 응답 쿠키로 설정합니다.
	 * 쿠키는 `accessToken` 이름으로, `HttpOnly`, `Secure` 속성을 가지며, `path`는 `/`로 설정되고 `maxAge`는 Access Token의 만료 시간(30분)으로 설정됩니다.
	 *
	 * @param response HttpServletResponse 객체, 쿠키를 추가하는 데 사용됩니다.
	 * @param accessToken 설정할 Access Token 문자열입니다.
	 * @author
	 */
	private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
		Cookie cookie = new Cookie("accessToken", accessToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)(jwtUtil.getAccessTokenExpiration() / 1000)); // 30분
		response.addCookie(cookie);
	}

	/**
	 * 설명: Refresh Token을 HTTP 응답 쿠키로 설정합니다.
	 * 쿠키는 `refreshToken` 이름으로, `HttpOnly`, `Secure` 속성을 가지며, `path`는 `/`로 설정되고 `maxAge`는 Refresh Token의 만료 시간(7일)으로 설정됩니다.
	 *
	 * @param response HttpServletResponse 객체, 쿠키를 추가하는 데 사용됩니다.
	 * @param refreshToken 설정할 Refresh Token 문자열입니다.
	 * @author
	 */
	private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie("refreshToken", refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)(jwtUtil.getRefreshTokenExpiration() / 1000)); // 7일
		response.addCookie(cookie);
	}

	/**
	 * 설명: Access Token 쿠키를 삭제합니다.
	 * `accessToken` 이름의 쿠키를 `maxAge`를 0으로 설정하여 즉시 만료시켜 삭제합니다.
	 *
	 * @param response HttpServletResponse 객체, 쿠키를 삭제하는 데 사용됩니다.
	 * @author
	 */
	private void clearAccessTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("accessToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // 즉시 만료
		response.addCookie(cookie);
	}

	/**
	 * 설명: Refresh Token 쿠키를 삭제합니다.
	 * `refreshToken` 이름의 쿠키를 `maxAge`를 0으로 설정하여 즉시 만료시켜 삭제합니다.
	 *
	 * @param response HttpServletResponse 객체, 쿠키를 삭제하는 데 사용됩니다.
	 * @author
	 */
	private void clearRefreshTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("refreshToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // 즉시 만료
		response.addCookie(cookie);
	}

	/**
	 * 설명: JSESSIONID를 삭제합니다.
	 * `JSESSIONID` 이름의 세션를 `maxAge`를 0으로 설정하여 즉시 만료시켜 삭제합니다.
	 *
	 * @param response HttpServletResponse 객체, 쿠키를 삭제하는 데 사용됩니다.
	 * @author
	 */
	private void clearJSessionIdCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("JSESSIONID", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // 즉시 만료
		response.addCookie(cookie);
	}

	/**
	 * 설명: 에러 응답을 위한 Map 객체를 생성합니다.
	 * `success` 필드를 `false`로 설정하고 `error` 필드에 에러 메시지를 포함합니다.
	 *
	 * @param message 에러 메시지 문자열입니다.
	 * @return Map<String, Object> 에러 정보를 포함하는 Map 객체입니다.
	 * @author
	 */
	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> error = new HashMap<>();
		error.put("success", false);
		error.put("error", message);
		return error;
	}

}
