package com.luti.mypage.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.luti.auth.entity.User;
import com.luti.auth.repository.RefreshTokenRepository;
import com.luti.auth.repository.UserRepository;
import com.luti.mypage.repository.RouteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 탈퇴한 사용자의 완전 삭제를 처리하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawScheduler {

	private final UserRepository userRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final RouteRepository routeRepository;

	/**
	 * 매일 새벽 2시에 7일이 지난 탈퇴 사용자들을 완전 삭제
	 * cron: 초 분 시 일 월 요일
	 */
	@Scheduled(cron = "0 0 */3 * * *")    // 매일 3시간 마다
	// @Scheduled(cron = "0 */5 * * * *")    // 5분마다
	@Transactional
	public void deleteWithdrawnUsers() {
		log.info("탈퇴 사용자 완전 삭제 스케줄러 시작");

		try {
			// 3시간 마다
			LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);

			List<User> usersToDelete = userRepository.findWithdrawnUsersOlderThan(threeHoursAgo);

			// // 5분 마다
			// LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
			// List<User> usersToDelete = userRepository.findWithdrawnUsersOlderThan(fiveMinutesAgo);

			if (usersToDelete.isEmpty()) {
				log.info("삭제할 탈퇴 사용자가 없습니다.");
				return;
			}

			log.info("삭제 대상 탈퇴 사용자 수: {}", usersToDelete.size());

			int deletedCount = 0;
			for (User user : usersToDelete) {
				try {
					deleteUserCompletely(user);
					deletedCount++;
					log.info("사용자 완전 삭제 완료 - ID: {}, 이메일: {}", user.getUserId(), user.getEmail());
				} catch (Exception e) {
					log.error("사용자 삭제 중 오류 발생 - ID: {}, 이메일: {}", user.getUserId(), user.getEmail(), e);
				}
			}

			log.info("탈퇴 사용자 완전 삭제 스케줄러 완료 - 삭제된 사용자 수: {}/{}", deletedCount, usersToDelete.size());

		} catch (Exception e) {
			log.error("탈퇴 사용자 삭제 스케줄러 실행 중 오류 발생", e);
		}
	}

	/**
	 * 사용자와 관련된 모든 데이터를 완전 삭제
	 *
	 * @param user 삭제할 사용자
	 */
	@Transactional
	protected void deleteUserCompletely(User user) {
		Long userId = user.getUserId();

		log.debug("사용자 완전 삭제 시작 - ID: {}", userId);

		// 1. Route의 user_id를 NULL로 변경 (콘텐츠는 보존)
		int updatedRoutes = routeRepository.setUserIdToNull(userId);
		log.debug("Route user_id NULL 처리 완료 - 대상 수: {}", updatedRoutes);

		// 2. RefreshToken 삭제
		int deletedTokens = refreshTokenRepository.deleteByUserId(userId);
		log.debug("RefreshToken 삭제 완료 - 대상 수: {}", deletedTokens);

		// 3. User 삭제
		userRepository.delete(user);
		log.debug("User 삭제 완료 - ID: {}", userId);
	}

	/**
	 * 테스트용 수동 실행 메서드 (개발 환경에서만 사용)
	 */
	public void executeManually() {
		log.info("탈퇴 사용자 삭제 스케줄러 수동 실행");
		deleteWithdrawnUsers();
	}

}
