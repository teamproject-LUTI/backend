package com.luti.mypage.controller;

import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.mypage.dto.request.MyPageProfileUpdateRequestDto;
import com.luti.mypage.dto.response.MyPageProfileResponseDto;
import com.luti.mypage.dto.response.ProfileImageResponseDto;
import com.luti.mypage.service.MyPageService;
import com.luti.mypage.service.ProfileImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 마이페이지 관련 API를 처리하는 컨트롤러 (프로필 이미지 기능 포함)
 */
@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageProfileController {

	private final MyPageService myPageService;
	private final ProfileImageService profileImageService;

	/**
	 * 마이페이지 메인 - 내 프로필 정보 조회
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
	 * 마이페이지 프로필 정보 수정 (텍스트 정보만)
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

		} catch (RuntimeException e) {
			log.error("프로필 수정 중 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("마이페이지 프로필 수정 중 서버 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("프로필 정보 수정에 실패했습니다."));
		}
	}

	/**
	 * 프로필 이미지 업로드/수정
	 */
	@PostMapping("/profile-image")
	public ResponseEntity<ProfileImageResponseDto> updateProfileImage(
			@RequestParam("profileImage") MultipartFile profileImage) {

		log.info("프로필 이미지 업로드 요청");

		try {
			Long userId = getCurrentUserId();

			// 파일 존재 여부 체크
			if (profileImage == null || profileImage.isEmpty()) {
				log.warn("업로드할 프로필 이미지가 없음");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(ProfileImageResponseDto.builder()
								.message("업로드할 프로필 이미지를 선택해주세요.")
								.build());
			}

			// 프로필 이미지 업로드 처리
			String imageUrl = profileImageService.updateProfileImage(userId, profileImage);

			log.info("프로필 이미지 업로드 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(ProfileImageResponseDto.success(imageUrl));

		} catch (RuntimeException e) {
			log.error("프로필 이미지 업로드 중 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ProfileImageResponseDto.builder()
							.message(e.getMessage())
							.build());
		} catch (Exception e) {
			log.error("프로필 이미지 업로드 중 서버 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ProfileImageResponseDto.builder()
							.message("프로필 이미지 업로드에 실패했습니다.")
							.build());
		}
	}

	/**
	 * 프로필 이미지 삭제 (기본 이미지로 변경)
	 */
	@DeleteMapping("/profile-image")
	public ResponseEntity<ProfileImageResponseDto> deleteProfileImage() {

		log.info("프로필 이미지 삭제 요청");

		try {
			Long userId = getCurrentUserId();

			// 프로필 이미지 삭제 처리
			profileImageService.deleteProfileImage(userId);

			log.info("프로필 이미지 삭제 성공 - 사용자 ID: {}", userId);
			return ResponseEntity.ok(ProfileImageResponseDto.deleted());

		} catch (RuntimeException e) {
			log.error("프로필 이미지 삭제 중 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ProfileImageResponseDto.builder()
							.message(e.getMessage())
							.build());
		} catch (Exception e) {
			log.error("프로필 이미지 삭제 중 서버 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ProfileImageResponseDto.builder()
							.message("프로필 이미지 삭제에 실패했습니다.")
							.build());
		}
	}

	/**
	 * 현재 프로필 이미지 정보 조회
	 */
	@GetMapping("/profile-image")
	public ResponseEntity<ProfileImageResponseDto> getCurrentProfileImage() {

		log.info("현재 프로필 이미지 조회 요청");

		try {
			Long userId = getCurrentUserId();

			// 현재 프로필 이미지 URL 조회
			String currentImageUrl = profileImageService.getCurrentProfileImageUrl(userId);

			return ResponseEntity.ok(ProfileImageResponseDto.current(currentImageUrl));

		} catch (RuntimeException e) {
			log.error("프로필 이미지 조회 중 비즈니스 로직 오류: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ProfileImageResponseDto.builder()
							.message(e.getMessage())
							.build());
		} catch (Exception e) {
			log.error("프로필 이미지 조회 중 서버 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ProfileImageResponseDto.builder()
							.message("프로필 이미지 조회에 실패했습니다.")
							.build());
		}
	}

	// === 유틸리티 메서드들 ===

	/**
	 * 현재 인증된 사용자 ID 추출
	 */
	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
			throw new RuntimeException("인증이 필요합니다.");
		}

		return jwtAuth.getCurrentUserId();
	}

	/**
	 * 에러 응답 생성 (기존 엔드포인트용)
	 */
	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> error = new HashMap<>();
		error.put("success", false);
		error.put("error", message);
		return error;
	}
}
