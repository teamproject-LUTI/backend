package com.luti.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로필 이미지 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileImageService {

	private final UserRepository userRepository;

	private final FileStorageService fileStorageService;

	/**
	 * 프로필 이미지 업로드/수정
	 *
	 * @param userId 사용자 ID
	 * @param profileImage 업로드할 프로필 이미지 파일
	 * @return 업로드된 이미지의 접근 URL
	 */
	@Transactional
	public String updateProfileImage(Long userId, MultipartFile profileImage) {
		log.info("프로필 이미지 수정 요청 - 사용자 ID: {}", userId);

		// 1. 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		// 2. 소셜 로그인 사용자 체크
		if (user.isSocialUser()) {
			throw new RuntimeException("소셜 로그인 사용자는 프로필 이미지를 직접 수정할 수 없습니다.");
		}

		try {
			// 3. 기존 프로필 이미지 삭제 (물리적 파일)
			if (user.getProfilePhysicalPath() != null) {
				fileStorageService.deleteProfileImage(user.getProfilePhysicalPath());
			}

			// 4. 새 프로필 이미지 저장
			FileStorageService.ProfileImageInfo imageInfo =
					fileStorageService.saveProfileImage(profileImage, userId);

			// 5. 사용자 엔티티에 이미지 정보 업데이트
			user.setProfileFileName(imageInfo.getFileName());
			user.setProfilePhysicalPath(imageInfo.getPhysicalPath());
			user.setProfileLogicalPath(imageInfo.getLogicalPath());
			user.setProfileExtension(imageInfo.getExtension());
			user.setProfileSize(imageInfo.getSize().intValue());

			// 6. 데이터베이스 저장
			userRepository.save(user);

			log.info("프로필 이미지 수정 완료 - 사용자 ID: {}, 파일: {}",
					userId, imageInfo.getFileName());

			return imageInfo.getAccessUrl();

		} catch (Exception e) {
			log.error("프로필 이미지 수정 중 오류 발생 - 사용자 ID: {}", userId, e);
			throw new RuntimeException("프로필 이미지 수정에 실패했습니다: " + e.getMessage());
		}
	}

	/**
	 * 프로필 이미지 삭제 (기본 이미지로 변경)
	 *
	 * @param userId 사용자 ID
	 */
	@Transactional
	public void deleteProfileImage(Long userId) {
		log.info("프로필 이미지 삭제 요청 - 사용자 ID: {}", userId);

		// 1. 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		// 2. 소셜 로그인 사용자 체크
		if (user.isSocialUser()) {
			throw new RuntimeException("소셜 로그인 사용자는 프로필 이미지를 직접 삭제할 수 없습니다.");
		}

		// 3. 현재 프로필 이미지가 없는 경우
		if (user.getProfilePhysicalPath() == null) {
			throw new RuntimeException("삭제할 프로필 이미지가 없습니다.");
		}

		try {
			// 4. 물리적 파일 삭제
			fileStorageService.deleteProfileImage(user.getProfilePhysicalPath());

			// 5. 사용자 엔티티에서 이미지 정보 제거
			user.setProfileFileName(null);
			user.setProfilePhysicalPath(null);
			user.setProfileLogicalPath(null);
			user.setProfileExtension(null);
			user.setProfileSize(null);

			// 6. 데이터베이스 저장
			userRepository.save(user);

			log.info("프로필 이미지 삭제 완료 - 사용자 ID: {}", userId);

		} catch (Exception e) {
			log.error("프로필 이미지 삭제 중 오류 발생 - 사용자 ID: {}", userId, e);
			throw new RuntimeException("프로필 이미지 삭제에 실패했습니다: " + e.getMessage());
		}
	}

	/**
	 * 현재 프로필 이미지 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @return 프로필 이미지 URL (없으면 null)
	 */
	@Transactional(readOnly = true)
	public String getCurrentProfileImageUrl(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		return user.getDisplayProfileImage();
	}

}
