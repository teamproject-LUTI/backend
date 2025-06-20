package com.luti.mypage.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.mypage.dto.request.PasswordUpdateRequestDto;
import com.luti.mypage.service.PasswordService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class PasswordController {

	private final PasswordService passwordService;

	/**
	 * 비밀번호 수정
	 *
	 * @param requestDto 비밀번호 수정 요청 정보
	 * @param bindingResult 유효성 검증 결과
	 * @param response HTTP 응답 객체 (쿠키 삭제용)
	 * @return ResponseEntity 수정 처리 결과
	 */
	@PostMapping("/password")
	public ResponseEntity<Map<String, Object>> updatePassword(
			@Valid @RequestBody PasswordUpdateRequestDto requestDto,
			BindingResult bindingResult,
			HttpServletResponse response) {

		log.info("비밀번호 수정 요청");

		// 1. 유효성 검증 실패 처리
		if (bindingResult.hasErrors()) {
			String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
			log.warn("비밀번호 수정 유효성 검증 실패: {}", errorMessage);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(errorMessage));
		}

		try {
			// 2. 현재 사용자 인증 정보 확인
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();

			// 3. 비밀번호 수정 처리
			passwordService.updatePassword(userId, requestDto);

			// 4. 보안상 모든 토큰 쿠키 삭제 (서비스에서 DB의 Refresh Token을 무효화했으므로)
			clearAllAuthCookies(response);

			// 5. 성공 응답
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("message", "비밀번호가 성공적으로 변경되었습니다. 보안을 위해 다시 로그인해주세요.");
			responseBody.put("requireReLogin", true); // 프론트엔드에서 로그인 페이지로 리다이렉트 하도록

			log.info("비밀번호 수정 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(responseBody);

		} catch (RuntimeException e) {
			log.warn("비밀번호 수정 실패 - 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("비밀번호 수정 중 서버 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("비밀번호 수정 중 오류가 발생했습니다."));
		}
	}

	/**
	 * 현재 비밀번호 검증 (비밀번호 수정 전 확인용)
	 *
	 * @param request 현재 비밀번호가 담긴 요청
	 * @return ResponseEntity 검증 결과
	 */
	@PostMapping("/password/verify")
	public ResponseEntity<Map<String, Object>> verifyCurrentPassword(
			@RequestBody Map<String, String> request) {

		log.info("현재 비밀번호 검증 요청");

		try {
			// 1. 현재 사용자 인증 정보 확인
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();
			String currentPassword = request.get("currentPassword");

			if (currentPassword == null || currentPassword.trim().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(createErrorResponse("현재 비밀번호를 입력해주세요."));
			}

			// 2. 비밀번호 검증
			boolean isValid = passwordService.verifyCurrentPassword(userId, currentPassword);

			// 3. 응답 생성
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("valid", isValid);

			if (isValid) {
				responseBody.put("message", "현재 비밀번호가 확인되었습니다.");
			} else {
				responseBody.put("message", "현재 비밀번호가 일치하지 않습니다.");
			}

			return ResponseEntity.ok(responseBody);

		} catch (RuntimeException e) {
			log.warn("비밀번호 검증 실패 - 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("비밀번호 검증 중 서버 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("비밀번호 검증 중 오류가 발생했습니다."));
		}
	}

	// ===== 유틸리티 메서드들 =====

	/**
	 * 모든 인증 관련 쿠키 삭제
	 */
	private void clearAllAuthCookies(HttpServletResponse response) {
		clearAccessTokenCookie(response);
		clearRefreshTokenCookie(response);
		clearJSessionIdCookie(response);
	}

	/**
	 * Access Token 쿠키 삭제
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
	 * Refresh Token 쿠키 삭제
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
	 * JSESSIONID 쿠키 삭제
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
	 * 에러 응답 생성
	 */
	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> error = new HashMap<>();
		error.put("success", false);
		error.put("error", message);
		return error;
	}

}
