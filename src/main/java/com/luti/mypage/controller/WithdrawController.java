package com.luti.mypage.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.mypage.dto.request.WithdrawRequestDto;
import com.luti.mypage.service.WithdrawService;

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
			responseBody.put("message", "회원탈퇴가 완료되었습니다. 3시간 이내 재로그인 시 계정을 복구할 수 있습니다.");

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
	 * 계정 복구 요청
	 *
	 * @return ResponseEntity 복구 처리 결과
	 */
	@PostMapping("/restore")
	public ResponseEntity<Map<String, Object>> restoreUser() {

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

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("message", "계정이 성공적으로 복구되었습니다.");

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

	// ===== 쿠키 관련 유틸리티 메서드들 =====

	private void clearAccessTokenCookie(HttpServletResponse response) {
		jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("accessToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private void clearRefreshTokenCookie(HttpServletResponse response) {
		jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("refreshToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private void clearJSessionIdCookie(HttpServletResponse response) {
		jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", "");
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
