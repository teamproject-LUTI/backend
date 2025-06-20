package com.luti.mypage.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.luti.auth.entity.User;
import com.luti.auth.repository.RefreshTokenRepository;
import com.luti.auth.repository.UserRepository;
import com.luti.mypage.dto.request.PasswordUpdateRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 비밀번호 수정
	 *
	 * @param userId 사용자 ID
	 * @param requestDto 비밀번호 수정 요청 정보
	 */
	@Transactional
	public void updatePassword(Long userId, PasswordUpdateRequestDto requestDto) {
		log.info("비밀번호 수정 요청 - 사용자 ID: {}", userId);

		// 1. 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		// 2. 소셜 로그인 사용자 체크
		if (user.isSocialUser()) {
			throw new RuntimeException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
		}

		// 3. 일반 로그인 사용자인지 확인 (비밀번호가 있는지)
		if (!user.hasPassword()) {
			throw new RuntimeException("비밀번호가 설정되지 않은 계정입니다.");
		}

		// 4. 새 비밀번호와 확인 비밀번호 일치 여부 확인
		if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
			throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
		}

		// 5. 현재 비밀번호 검증
		if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
			throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
		}

		// 6. 새 비밀번호가 현재 비밀번호와 같은지 확인
		if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
			throw new RuntimeException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
		}

		// 7. 새 비밀번호 암호화 및 저장
		String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());
		user.setPassword(encodedNewPassword);
		userRepository.save(user);

		// 8. 보안상 모든 디바이스에서 로그아웃 처리 (모든 Refresh Token 무효화)
		refreshTokenRepository.revokeAllByUser(user);

		log.info("비밀번호 수정 완료 - 사용자 ID: {}, 모든 디바이스 로그아웃 처리됨", userId);
	}

	/**
	 * 현재 비밀번호 검증만 수행 (비밀번호 수정 전 확인용)
	 *
	 * @param userId 사용자 ID
	 * @param currentPassword 현재 비밀번호
	 * @return 검증 결과
	 */
	@Transactional(readOnly = true)
	public boolean verifyCurrentPassword(Long userId, String currentPassword) {
		log.debug("현재 비밀번호 검증 요청 - 사용자 ID: {}", userId);

		try {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

			// 소셜 로그인 사용자 체크
			if (user.isSocialUser()) {
				throw new RuntimeException("소셜 로그인 사용자는 비밀번호 검증을 할 수 없습니다.");
			}

			// 비밀번호가 있는지 확인
			if (!user.hasPassword()) {
				throw new RuntimeException("비밀번호가 설정되지 않은 계정입니다.");
			}

			// 비밀번호 검증
			boolean isValid = passwordEncoder.matches(currentPassword, user.getPassword());
			log.debug("현재 비밀번호 검증 결과 - 사용자 ID: {}, 결과: {}", userId, isValid);

			return isValid;

		} catch (Exception e) {
			log.error("비밀번호 검증 중 오류 발생 - 사용자 ID: {}, 오류: {}", userId, e.getMessage());
			return false;
		}
	}

}
