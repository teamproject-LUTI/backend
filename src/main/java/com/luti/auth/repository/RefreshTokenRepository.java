package com.luti.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.luti.auth.entity.RefreshToken;
import com.luti.auth.entity.User;

/**
 * 설명: RefreshToken 엔티티에 대한 데이터베이스 접근을 위한 Spring Data JPA 리포지토리 인터페이스입니다.
 * Refresh Token의 생성, 조회, 수정, 삭제 및 특정 조건에 따른 쿼리 기능을 제공합니다.
 *
 * @author
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	/**
	 * 설명: 주어진 토큰 값과 일치하는 RefreshToken 엔티티를 조회합니다.
	 *
	 * @param tokenValue 조회할 Refresh Token의 문자열 값.
	 * @return Optional<RefreshToken> 해당 토큰 값을 가진 RefreshToken 엔티티 (존재하지 않으면 Optional.empty()).
	 * @author
	 */
	Optional<RefreshToken> findByTokenValue(String tokenValue);

	/**
	 * 설명: 특정 사용자가 소유한 모든 RefreshToken을 무효화(isRevoked = true) 상태로 업데이트합니다.
	 * 이 메서드는 데이터를 수정하므로 @Modifying 어노테이션이 필요합니다.
	 *
	 * @param user 무효화할 Refresh Token을 소유한 User 엔티티.
	 * @author
	 */
	@Modifying
	@Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
	void revokeAllByUser(@Param("user") User user);

	/**
	 * 설명: 만료되었거나(expiresAt이 현재 시간 이전) 무효화된(isRevoked가 true) RefreshToken들을 데이터베이스에서 삭제합니다.
	 * 이 메서드는 데이터를 삭제하므로 @Modifying 어노테이션이 필요합니다.
	 *
	 * @param now 현재 시간 (만료 여부를 판단하는 기준).
	 * @author
	 */
	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.isRevoked = true")
	void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

}
