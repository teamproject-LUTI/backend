package com.luti.mypage.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luti.auth.entity.RefreshToken;
import com.luti.auth.entity.User;
import com.luti.auth.repository.RefreshTokenRepository;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.auth.util.JwtUtil;
import com.luti.mypage.dto.request.WithdrawRequestDto;
import com.luti.mypage.service.WithdrawService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원탈퇴 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class WithdrawController {

	private final WithdrawService withdrawService;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * 회원탈퇴 요청
	 *
	 * @param requestDto 탈퇴 요청 정보 (일반 로그인 시 비밀번호 포함)
	 * @param response HTTP 응답 객체 (쿠키 삭제용)
	 * @return ResponseEntity 탈퇴 처리 결과
	 */
	@DeleteMapping("/withdraw")
	public ResponseEntity<Map<String, Object>> withdrawUser(
			@RequestBody WithdrawRequestDto requestDto,
			HttpServletResponse response) {

		log.info("회원탈퇴 요청");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();
			String provider = jwtAuth.getCurrentUserProvider();

			// 탈퇴 처리
			withdrawService.withdrawUser(userId, provider, requestDto.getPassword());

			// 쿠키 삭제
			clearAccessTokenCookie(response);
			clearRefreshTokenCookie(response);
			clearJSessionIdCookie(response);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("message", "회원탈퇴가 완료되었습니다. 5분 이내 재로그인 시 계정을 복구할 수 있습니다.");

			log.info("회원탈퇴 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(responseBody);

		} catch (IllegalArgumentException e) {
			log.warn("회원탈퇴 실패 - 잘못된 요청: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("회원탈퇴 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("회원탈퇴 처리 중 오류가 발생했습니다."));
		}
	}

	/**
	 * 계정 복구 요청 (정상 토큰 재발급 포함)
	 *
	 * @return ResponseEntity 복구 처리 결과
	 */
	@PostMapping("/restore")
	@Transactional
	public ResponseEntity<Map<String, Object>> restoreUser(
			HttpServletRequest request,
			HttpServletResponse response) {

		log.info("계정 복구 요청");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();

			// 계정 복구 처리
			withdrawService.restoreUser(userId);

			// 복구 후 사용자 정보 다시 조회
			User restoredUser = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("복구된 사용자를 찾을 수 없습니다."));

			// 정상 토큰 재발급
			String newAccessToken = jwtUtil.generateAccessToken(
					restoredUser.getUserId(),
					restoredUser.getEmail(),
					restoredUser.getDisplayName(),
					restoredUser.getNickname(),
					restoredUser.getDisplayProfileImage(),
					restoredUser.getUserTypeId() != null ? restoredUser.getUserTypeId().getUserTypeId() : 1L,
					restoredUser.getProvider()
			);

			String newRefreshToken = jwtUtil.generateRefreshToken(restoredUser.getUserId());

			// 기존 임시 토큰 삭제 및 새 Refresh Token DB에 저장
			refreshTokenRepository.deleteByUserId(userId);
			saveRefreshToken(restoredUser, newRefreshToken, request);

			// 새 토큰을 쿠키로 설정
			setAccessTokenCookie(response, newAccessToken);
			setRefreshTokenCookie(response, newRefreshToken);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("message", "계정이 성공적으로 복구되었습니다.");
			responseBody.put("redirectTo", "/main"); // 프론트엔드에서 메인 페이지로 리다이렉트할 수 있도록

			log.info("계정 복구 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(responseBody);

		} catch (IllegalArgumentException e) {
			log.warn("계정 복구 실패 - 잘못된 요청: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("계정 복구 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("계정 복구 처리 중 오류가 발생했습니다."));
		}
	}

	/**
	 * 탈퇴 계정 상태 확인
	 * JWT 필터에서 탈퇴한 계정 감지 시 호출될 수 있는 API
	 *
	 * @return ResponseEntity 탈퇴 계정 정보
	 */
	@GetMapping("/withdraw/status")
	public ResponseEntity<Map<String, Object>> getWithdrawStatus() {

		log.info("탈퇴 계정 상태 확인 요청");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();

			// 탈퇴 상태 정보 조회
			Map<String, Object> withdrawInfo = withdrawService.getWithdrawStatus(userId);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("withdrawInfo", withdrawInfo);

			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			log.error("탈퇴 상태 확인 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("상태 확인 중 오류가 발생했습니다."));
		}
	}

	// ===== 토큰 관련 유틸리티 메서드들 =====

	/**
	 * Refresh Token을 DB에 저장
	 */
	@Transactional
	protected void saveRefreshToken(User user, String refreshToken, HttpServletRequest request) {
		try {
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

	/**
	 * Access Token을 HttpOnly 쿠키로 설정
	 */
	private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
		Cookie cookie = new Cookie("accessToken", accessToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)(jwtUtil.getAccessTokenExpiration() / 1000)); // 30분
		response.addCookie(cookie);
		log.debug("Access Token 쿠키 설정 완료");
	}

	/**
	 * Refresh Token을 HttpOnly 쿠키로 설정
	 */
	private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie("refreshToken", refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)(jwtUtil.getRefreshTokenExpiration() / 1000)); // 7일
		response.addCookie(cookie);
		log.debug("Refresh Token 쿠키 설정 완료");
	}

	// ===== 쿠키 관련 유틸리티 메서드들 =====

	private void clearAccessTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("accessToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private void clearRefreshTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("refreshToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private void clearJSessionIdCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("JSESSIONID", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> error = new HashMap<>();
		error.put("success", false);
		error.put("error", message);
		return error;
	}

}
