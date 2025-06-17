package com.luti.mypage.controller;

import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.mypage.dto.MyPageProfileResponseDto;
import com.luti.mypage.dto.request.MyPageProfileUpdateRequestDto;
import com.luti.mypage.service.MyPageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 마이페이지 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageProfileController {

	private final MyPageService myPageService;

	/**
	 * 마이페이지 메인 - 내 프로필 정보 조회
	 *
	 * @return ResponseEntity 마이페이지 프로필 정보
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> getMyPage() {
		log.info("마이페이지 조회 요청");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();
			MyPageProfileResponseDto profile = myPageService.getMyPageProfile(userId);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("profile", profile);

			log.info("마이페이지 조회 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("마이페이지 조회 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("프로필 정보 조회에 실패했습니다."));
		}
	}

	/**
	 * 마이페이지 프로필 정보 수정
	 * PUT 요청은 리소스 전체를 업데이트할 때 사용합니다. (부분 업데이트 시 PATCH)
	 *
	 * @param requestDto 수정할 프로필 정보
	 * @return ResponseEntity 수정 결과
	 */
	@PutMapping("/update")
	public ResponseEntity<Map<String, Object>> updateMyPageProfile(@RequestBody MyPageProfileUpdateRequestDto requestDto) {
		log.info("마이페이지 프로필 수정 요청: {}", requestDto);

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("인증이 필요합니다."));
			}

			Long userId = jwtAuth.getCurrentUserId();
			myPageService.updateMyPageProfile(userId, requestDto);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "프로필 정보가 성공적으로 수정되었습니다.");

			log.info("마이페이지 프로필 수정 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(response);

		} catch (RuntimeException e) { // 서비스 계층에서 발생할 수 있는 사용자 Not Found 예외 등
			log.error("프로필 수정 중 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400 Bad Request 또는 404 Not Found
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("마이페이지 프로필 수정 중 서버 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("프로필 정보 수정에 실패했습니다."));
		}
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
