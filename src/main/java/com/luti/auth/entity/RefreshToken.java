package com.luti.auth.entity;

import java.time.LocalDateTime;

import com.luti.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 설명: 사용자의 Refresh Token 정보를 데이터베이스에 관리하는 JPA 엔티티입니다.
 * Access Token 갱신에 사용되며, 만료 시간, 무효화 상태 및 디바이스 정보 등을 포함합니다.
 * `Auditable`을 상속하여 생성 및 수정 시간을 자동으로 기록합니다.
 *
 */
@Entity
@Table(name = "refreshToken")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 엔티티 요구사항: 기본 생성자 (접근 레벨 보호)
public class RefreshToken extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증분 기본 키 전략
	@Column(name = "token_id")
	private Long tokenId; // Refresh Token의 고유 식별자

	@ManyToOne(fetch = FetchType.LAZY) // User 엔티티와의 Many-to-One 관계 (지연 로딩)
	@JoinColumn(name = "user_id", nullable = false) // user 테이블의 기본 키(id)에 매핑되는 외래 키
	private User user; // 이 Refresh Token이 속한 사용자

	@Column(name = "token_value", nullable = false, length = 500)
	private String tokenValue; // 실제 Refresh Token 문자열

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt; // Refresh Token의 만료 시간

	@Column(name = "is_revoked", nullable = false)
	private Boolean isRevoked = false; // 토큰이 명시적으로 무효화되었는지 여부 (기본값: false)

	@Column(name = "device_info", length = 200)
	private String deviceInfo; // 토큰이 발급된 디바이스에 대한 정보 (선택 사항)

	/**
	 * 설명: RefreshToken 객체를 생성하기 위한 빌더 패턴 생성자입니다.
	 * 외부에서 직접 객체를 생성하는 대신 빌더를 사용하도록 유도합니다.
	 *
	 * @param user Refresh Token을 소유한 User 엔티티
	 * @param tokenValue Refresh Token의 실제 값
	 * @param expiresAt Refresh Token의 만료 시간
	 * @param deviceInfo 토큰이 발급된 디바이스 정보 (선택 사항)
	 * @author
	 */
	@Builder
	private RefreshToken(User user, String tokenValue, LocalDateTime expiresAt, String deviceInfo) {
		this.user = user;
		this.tokenValue = tokenValue;
		this.expiresAt = expiresAt;
		this.deviceInfo = deviceInfo;
		this.isRevoked = false; // 빌더를 통해 생성 시 초기 무효화 상태는 false
	}

	/**
	 * 설명: 해당 Refresh Token을 무효화(revoked) 상태로 변경합니다.
	 * `isRevoked` 필드를 `true`로 설정하여 더 이상 유효하지 않음을 나타냅니다.
	 *
	 * @author
	 */
	public void revoke() {
		this.isRevoked = true;
	}

	/**
	 * 설명: Refresh Token의 만료 여부를 확인합니다.
	 * 현재 시간이 토큰의 만료 시간(`expiresAt`) 이후인지 비교합니다.
	 *
	 * @return boolean 현재 시간이 `expiresAt` 이후이면 `true`, 아니면 `false`를 반환합니다.
	 * @author
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt);
	}

	/**
	 * 설명: Refresh Token의 유효성(만료되지 않고 무효화되지 않음)을 확인합니다.
	 * `isExpired()`와 `isRevoked` 필드 값을 모두 고려하여 유효성을 판단합니다.
	 *
	 * @return boolean 토큰이 만료되지 않았고 무효화되지 않았다면 `true`, 그렇지 않으면 `false`를 반환합니다.
	 * @author
	 */
	public boolean isValid() {
		return !isExpired() && !isRevoked;
	}

	/**
	 * 설명: Refresh Token의 값을 새로운 값과 새로운 만료 시간으로 업데이트합니다.
	 * 토큰을 재사용할 경우 `isRevoked` 상태를 `false`로 초기화합니다.
	 *
	 * @param newTokenValue 새로 업데이트할 Refresh Token 문자열 값
	 * @param newExpiresAt 새로 업데이트할 Refresh Token의 만료 시간
	 * @author
	 */
	public void updateToken(String newTokenValue, LocalDateTime newExpiresAt) {
		this.tokenValue = newTokenValue;
		this.expiresAt = newExpiresAt;
		this.isRevoked = false; // 토큰 업데이트 시 무효화 상태 초기화
	}

}
