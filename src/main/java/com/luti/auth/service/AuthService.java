package com.luti.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.luti.auth.entity.RefreshToken;
import com.luti.auth.entity.User;
import com.luti.auth.repository.RefreshTokenRepository;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 설명: 인증(Authentication)과 관련된 핵심 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * JWT 토큰 갱신, 사용자 로그아웃, 만료된 토큰 정리 등의 기능을 제공합니다.
 *
 * @author
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtUtil jwtUtil;

	private final UserRepository userRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * 설명: 제공된 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급(갱신)합니다.
	 * Refresh Token의 유효성 검증, 소유자 확인, DB에 저장된 토큰 상태 확인 등 일련의 과정을 거쳐
	 * 안전하게 토큰을 갱신하고, 토큰 로테이션(Token Rotation)을 구현합니다.
	 * 임시 토큰의 경우 임시 토큰으로만 갱신됩니다.
	 *
	 * @param refreshTokenValue 클라이언트로부터 전달받은 Refresh Token 문자열.
	 * @return TokenRefreshResult 토큰 갱신 성공 여부와 새로 발급된 토큰들을 담은 결과 객체.
	 * @author
	 */
	@Transactional
	public TokenRefreshResult refreshAccessToken(String refreshTokenValue) {
		log.info("Access Token 갱신 요청");

		try {
			// 1. Refresh Token의 형식적 유효성 검증 (서명, 만료 등)
			if (!jwtUtil.validateToken(refreshTokenValue)) {
				log.warn("유효하지 않은 Refresh Token");
				return TokenRefreshResult.failure("유효하지 않은 토큰입니다.");
			}

			// 2. 토큰 타입 확인
			String tokenType = jwtUtil.getTokenType(refreshTokenValue);
			boolean isTempToken = "TEMP_REFRESH".equals(tokenType);

			if (!isTempToken && !"REFRESH".equals(tokenType)) {
				log.warn("잘못된 토큰 타입: {}", tokenType);
				return TokenRefreshResult.failure("잘못된 토큰 타입입니다.");
			}

			// 3. Refresh Token에서 사용자 ID(subject) 추출
			Long userId = jwtUtil.getUserIdFromToken(refreshTokenValue);

			// 4. 추출된 사용자 ID로 DB에서 사용자 정보 조회
			User user = userRepository.findById(userId)
					.orElse(null);

			if (user == null) {
				log.warn("사용자를 찾을 수 없음: {}", userId);
				return TokenRefreshResult.failure("사용자를 찾을 수 없습니다.");
			}

			// 5. 임시 토큰이 아닌 경우에만 DB 토큰 검증
			if (!isTempToken) {
				// DB에 저장된 Refresh Token 엔티티 조회
				Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenValue(refreshTokenValue);

				if (refreshTokenOpt.isEmpty()) {
					log.warn("DB에서 Refresh Token을 찾을 수 없음");
					return TokenRefreshResult.failure("토큰을 찾을 수 없습니다.");
				}

				RefreshToken refreshToken = refreshTokenOpt.get();

				// 조회된 Refresh Token 엔티티의 유효성 검증
				if (!refreshToken.isValid()) {
					log.warn("만료되거나 무효화된 Refresh Token");
					return TokenRefreshResult.failure("만료되거나 무효화된 토큰입니다.");
				}

				// 토큰 소유자 확인
				if (!refreshToken.getUser().getUserId().equals(userId)) {
					log.warn("토큰 소유자 불일치 - 요청 사용자 ID: {}, 토큰 사용자 ID: {}", userId, refreshToken.getUser().getUserId());
					return TokenRefreshResult.failure("권한이 없습니다.");
				}
			}

			// 6. 새로운 토큰 생성 (임시 토큰이면 임시 토큰으로, 일반 토큰이면 일반 토큰으로)
			String newAccessToken;
			String newRefreshToken;

			if (isTempToken) {
				// 탈퇴 상태가 아니면 임시 토큰 갱신 거부
				if (!"Y".equals(user.getWithdrawYn())) {
					log.info("이미 복구된 계정의 임시 토큰 갱신 시도 - 사용자 ID: {}", userId);
					return TokenRefreshResult.failure("이미 복구된 계정입니다. 다시 로그인해주세요.");
				}

				// 임시 토큰 갱신
				newAccessToken = jwtUtil.generateTempAccessToken(
						user.getUserId(),
						user.getEmail(),
						user.getDisplayName(),
						user.getNickname(),
						user.getDisplayProfileImage(),
						user.getUserTypeId() != null ? user.getUserTypeId().getUserTypeId() : 1L,
						user.getProvider()
				);

				newRefreshToken = jwtUtil.generateTempRefreshToken(user.getUserId());

				log.info("임시 토큰 갱신 성공 - 사용자 ID: {}", userId);
			} else {
				// 탈퇴 상태면 일반 토큰 갱신 거부
				if ("Y".equals(user.getWithdrawYn())) {
					log.warn("탈퇴한 사용자의 일반 토큰 갱신 시도 - 사용자 ID: {}", userId);
					return TokenRefreshResult.failure("탈퇴한 계정입니다.");
				}

				// 일반 토큰 갱신
				newAccessToken = jwtUtil.generateAccessToken(
						user.getUserId(),
						user.getEmail(),
						user.getDisplayName(),
						user.getNickname(),
						user.getDisplayProfileImage(),
						user.getUserTypeId() != null ? user.getUserTypeId().getUserTypeId() : 1L,
						user.getProvider()
				);

				newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId());

				// DB의 Refresh Token 업데이트
				Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenValue(refreshTokenValue);
				if (refreshTokenOpt.isPresent()) {
					RefreshToken refreshToken = refreshTokenOpt.get();
					LocalDateTime newExpiresAt = LocalDateTime.now()
							.plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000);

					refreshToken.updateToken(newRefreshToken, newExpiresAt);
					refreshTokenRepository.save(refreshToken);
				}

				log.info("일반 토큰 갱신 성공 - 사용자 ID: {}", userId);
			}

			return TokenRefreshResult.success(newAccessToken, newRefreshToken);

		} catch (Exception e) {
			log.error("토큰 갱신 중 예외 발생: {}", e.getMessage(), e);
			return TokenRefreshResult.failure("토큰 갱신에 실패했습니다.");
		}
	}

	/**
	 * 설명: 사용자의 로그아웃을 처리합니다.
	 * 특정 Refresh Token 값을 제공하면 해당 토큰만 무효화하고,
	 * Refresh Token 값이 제공되지 않으면 해당 사용자의 모든 Refresh Token을 무효화하여 모든 디바이스에서 로그아웃시킵니다.
	 *
	 * @param userId 로그아웃을 요청한 사용자의 ID.
	 * @param refreshTokenValue (선택 사항) 무효화할 특정 Refresh Token 문자열. null이면 해당 사용자의 모든 토큰 무효화.
	 * @author
	 */
	@Transactional
	public void logout(Long userId, String refreshTokenValue) {
		log.info("로그아웃 처리 시작 - 사용자 ID: {}", userId);

		try {
			if (refreshTokenValue != null) {
				// 임시 토큰인지 확인
				if (jwtUtil.isTempToken(refreshTokenValue)) {
					log.info("임시 토큰 로그아웃 - 사용자 ID: {}", userId);
					// 임시 토큰은 DB에 저장되지 않으므로 별도 처리 없음
				} else {
					// 특정 Refresh Token만 DB에서 조회하여 무효화
					refreshTokenRepository.findByTokenValue(refreshTokenValue)
							.ifPresent(RefreshToken::revoke);
					log.info("단일 Refresh Token 무효화 완료 - 사용자 ID: {}", userId);
				}
			} else {
				// 해당 사용자의 모든 Refresh Token을 무효화
				User user = userRepository.findById(userId).orElse(null);
				if (user != null) {
					refreshTokenRepository.revokeAllByUser(user);
					log.info("사용자({})의 모든 Refresh Token 무효화 완료", userId);
				} else {
					log.warn("로그아웃할 사용자를 찾을 수 없음: {}", userId);
				}
			}

			log.info("로그아웃 처리 완료 - 사용자 ID: {}", userId);

		} catch (Exception e) {
			log.error("로그아웃 처리 중 예외 발생: {}", e.getMessage(), e);
		}
	}

	/**
	 * 설명: 특정 사용자의 모든 Refresh Token을 무효화하여 해당 사용자가 로그인된 모든 디바이스에서 강제로 로그아웃시킵니다.
	 * 이는 보안 침해 상황이나 비밀번호 변경 시 유용하게 사용될 수 있습니다.
	 *
	 * @param userId 모든 토큰을 무효화할 사용자의 ID.
	 * @author
	 */
	@Transactional
	public void logoutFromAllDevices(Long userId) {
		log.info("모든 디바이스에서 로그아웃 요청 - 사용자 ID: {}", userId);

		try {
			User user = userRepository.findById(userId).orElse(null);
			if (user != null) {
				refreshTokenRepository.revokeAllByUser(user);
				log.info("모든 디바이스 로그아웃 완료 - 사용자 ID: {}", userId);
			} else {
				log.warn("모든 디바이스에서 로그아웃할 사용자를 찾을 수 없음: {}", userId);
			}
		} catch (Exception e) {
			log.error("모든 디바이스 로그아웃 처리 중 예외 발생: {}", e.getMessage(), e);
		}
	}

	/**
	 * 설명: 데이터베이스에 저장된 만료되었거나 이미 무효화된 Refresh Token들을 주기적으로 정리(삭제)합니다.
	 * 이 메서드는 주로 스케줄러(예: `@Scheduled` 어노테이션)에 의해 호출되어 DB의 불필요한 데이터를 제거하고 성능을 유지합니다.
	 *
	 * @author
	 */
	@Transactional
	public void cleanupExpiredTokens() {
		log.info("만료된 토큰 정리 시작");

		try {
			refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
			log.info("만료된 토큰 정리 완료");
		} catch (Exception e) {
			log.error("만료된 토큰 정리 중 예외 발생: {}", e.getMessage(), e);
		}
	}

	/**
	 * 설명: 토큰 갱신 작업의 결과를 캡슐화하는 내부 정적 클래스입니다.
	 * 갱신 성공 여부, 새로 발급된 Access Token과 Refresh Token, 그리고 실패 시 에러 메시지를 포함합니다.
	 */
	public static class TokenRefreshResult {

		private final boolean success;
		private final String accessToken;
		private final String refreshToken;
		private final String errorMessage;

		private TokenRefreshResult(boolean success, String accessToken, String refreshToken, String errorMessage) {
			this.success = success;
			this.accessToken = accessToken;
			this.refreshToken = refreshToken;
			this.errorMessage = errorMessage;
		}

		public static TokenRefreshResult success(String accessToken, String refreshToken) {
			return new TokenRefreshResult(true, accessToken, refreshToken, null);
		}

		public static TokenRefreshResult failure(String errorMessage) {
			return new TokenRefreshResult(false, null, null, errorMessage);
		}

		public boolean isSuccess() {
			return success;
		}

		public String getAccessToken() {
			return accessToken;
		}

		public String getRefreshToken() {
			return refreshToken;
		}

		public String getErrorMessage() {
			return errorMessage;
		}
	}
}
