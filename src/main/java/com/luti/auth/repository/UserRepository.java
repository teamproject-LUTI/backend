package com.luti.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.luti.auth.entity.User;

/**
 * 설명: User 엔티티에 대한 데이터베이스 접근을 위한 Spring Data JPA 리포지토리 인터페이스입니다.
 * 사용자 정보를 조회하고 관리하는 다양한 쿼리 메서드를 제공합니다.
 *
 * @author
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * 설명: 주어진 이메일 주소와 일치하는 User 엔티티를 조회합니다.
	 *
	 * @param email 조회할 사용자의 이메일 주소.
	 * @return Optional<User> 해당 이메일을 가진 User 엔티티 (존재하지 않으면 Optional.empty()).
	 * @author
	 */
	Optional<User> findByEmail(String email);

	/**
	 * 설명: 소셜 제공자(provider)와 소셜 ID(socialId)를 기반으로 User 엔티티를 조회합니다.
	 * 이메일 대신 `birthday` 컬럼에 소셜 제공자 정보를, `profileExtension` 컬럼에 소셜 ID를 저장하여 활용합니다.
	 * 또한, 소셜 로그인 사용자를 식별하기 위해 `password` 컬럼 값이 'SOCIAL_LOGIN'인 경우만 조회합니다.
	 *
	 * @param provider 소셜 로그인 제공자명 (예: "google", "kakao", "naver"). 이는 User 엔티티의 `birthday` 필드에 매핑됩니다.
	 * @param socialId 소셜 로그인 제공자로부터 받은 사용자의 고유 ID. 이는 User 엔티티의 `profileExtension` 필드에 매핑됩니다.
	 * @return Optional<User> 해당 조건에 맞는 User 엔티티 (존재하지 않으면 Optional.empty()).
	 * @author
	 */
	@Query("SELECT u FROM User u WHERE u.birthday = :provider AND u.profileExtension = :socialId AND u.password = 'SOCIAL_LOGIN'")
	Optional<User> findBySocialProviderAndSocialId(@Param("provider") String provider,
			@Param("socialId") String socialId);

	/**
	 * 설명: 주어진 사용자 ID가 소셜 로그인 사용자인지 여부를 확인합니다.
	 * `password` 필드가 'SOCIAL_LOGIN' 문자열인 경우 소셜 로그인 사용자로 간주합니다.
	 *
	 * @param userId 확인할 사용자의 고유 ID.
	 * @return boolean 해당 사용자 ID가 소셜 로그인 사용자인 경우 `true`, 그렇지 않으면 `false`.
	 * @author
	 */
	@Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userId = :userId AND u.password = 'SOCIAL_LOGIN'")
	boolean isSocialUser(@Param("userId") Long userId);

	/**
	 * 설명: 주어진 닉네임이 이미 데이터베이스에 존재하는지 여부를 확인합니다.
	 *
	 * @param nickname 확인할 닉네임.
	 * @return boolean 해당 닉네임이 이미 존재하면 `true`, 그렇지 않으면 `false`.
	 * @author
	 */
	boolean existsByNickname(String nickname);

}
