package com.luti.mypage.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원탈퇴 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원탈퇴 처리
	 *
	 * @param userId 탈퇴할 사용자 ID
	 * @param provider 로그인 제공자 (LOCAL, GOOGLE 등)
	 * @param password 비밀번호 (일반 로그인 사용자만)
	 */
	@Transactional
	public void withdrawUser(Long userId, String provider, String password) {
		log.info("회원탈퇴 처리 시작 - 사용자 ID: {}, 제공자: {}", userId, provider);

		// 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 이미 탈퇴한 사용자 확인
		if ("Y".equals(user.getWithdrawYn())) {
			throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
		}

		// 일반 로그인 사용자의 경우 비밀번호 검증
		if ("LOCAL".equals(provider) && user.hasPassword()) {
			validatePassword(user, password);
		}

		// 탈퇴 처리 (withdrawYn을 'Y'로 변경)
		user.setWithdrawYn("Y");
		userRepository.save(user);

		log.info("회원탈퇴 처리 완료 - 사용자 ID: {}", userId);
	}

	/**
	 * 계정 복구 처리
	 *
	 * @param userId 복구할 사용자 ID
	 */
	@Transactional
	public void restoreUser(Long userId) {
		log.info("계정 복구 처리 시작 - 사용자 ID: {}", userId);

		// 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 탈퇴한 계정이 아닌 경우
		if (!"Y".equals(user.getWithdrawYn())) {
			throw new IllegalArgumentException("탈퇴한 계정이 아닙니다.");
		}

		// 복구 처리 (withdrawYn을 'N'으로 변경)
		user.setWithdrawYn("N");
		userRepository.save(user);

		log.info("계정 복구 처리 완료 - 사용자 ID: {}", userId);
	}

	/**
	 * 탈퇴 계정 상태 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @return 탈퇴 상태 정보
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> getWithdrawStatus(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Map<String, Object> statusInfo = new HashMap<>();
		statusInfo.put("isWithdrawn", "Y".equals(user.getWithdrawYn()));
		statusInfo.put("withdrawDate", user.getModifiedAt());

		if ("Y".equals(user.getWithdrawYn()) && user.getModifiedAt() != null) {
			// 삭제 예정일 계산 (탈퇴일로부터 7일 후)
			// LocalDateTime deleteDate = user.getModifiedAt().plusDays(7);

			// 3시간
			LocalDateTime deleteDate = user.getModifiedAt().plusHours(3);
			statusInfo.put("deleteDate", deleteDate);
			statusInfo.put("deleteDateFormatted", deleteDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

			// 남은 일수 계산
			long daysRemaining = java.time.Duration.between(LocalDateTime.now(), deleteDate).toDays();
			statusInfo.put("daysRemaining", Math.max(0, daysRemaining));
		}

		return statusInfo;
	}

	/**
	 * 일반 로그인 사용자의 비밀번호 검증
	 *
	 * @param user 사용자 엔티티
	 * @param inputPassword 입력받은 비밀번호
	 */
	private void validatePassword(User user, String inputPassword) {
		if (inputPassword == null || inputPassword.trim().isEmpty()) {
			throw new IllegalArgumentException("비밀번호를 입력해주세요.");
		}

		if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		log.debug("비밀번호 검증 성공 - 사용자 ID: {}", user.getUserId());
	}

}
