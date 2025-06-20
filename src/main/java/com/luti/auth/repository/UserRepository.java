package com.luti.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	@Query("SELECT u FROM User u WHERE u.provider = :provider AND u.profileExtension = :socialId AND u.password = 'SOCIAL_LOGIN'")
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

	boolean existsByUserId(Long userId);

	boolean existsByEmail(String email);

	User findByUserId(Long userId);

	/**
	 * 탈퇴 사용자 조회
	 *
	 * @param dateTime 기준 날짜
	 * @return 삭제 대상 탈퇴 사용자 목록
	 */
	@Query("SELECT u FROM User u WHERE u.withdrawYn = 'Y' AND u.modifiedAt < :dateTime")
	List<User> findWithdrawnUsersOlderThan(@Param("dateTime") LocalDateTime dateTime);

	/**
	 * 탈퇴한 사용자인지 확인
	 *
	 * @param userId 사용자 ID
	 * @return 탈퇴 여부
	 */
	@Query("SELECT u.withdrawYn FROM User u WHERE u.userId = :userId")
	String getWithdrawStatus(@Param("userId") Long userId);

	/**
	 * 모든 사용자를 생성일 역순으로 조회 (페이징)
	 */
	@Query("SELECT u FROM User u WHERE u.withdrawYn != 'Y' OR u.withdrawYn IS NULL ORDER BY u.createdAt DESC")
	Page<User> findAllActiveUsers(Pageable pageable);

	/**
	 * 특정 사용자 타입의 사용자들을 페이징으로 조회
	 * UserType은 연관관계이므로 JOIN 쿼리 사용
	 */
	@Query("SELECT u FROM User u WHERE u.userTypeId.userTypeId = :userTypeId AND (u.withdrawYn != 'Y' OR u.withdrawYn IS NULL)")
	Page<User> findByUserTypeIdAndNotWithdrawn(@Param("userTypeId") Long userTypeId, Pageable pageable);

	/**
	 * 사용자 검색 (이름, 이메일, 닉네임으로 검색) - 페이징 지원
	 */
	@Query("SELECT u FROM User u WHERE (u.withdrawYn != 'Y' OR u.withdrawYn IS NULL) AND " +
			"(LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<User> searchActiveUsers(@Param("keyword") String keyword, Pageable pageable);


	/**
	 * 활성 사용자 수 조회 (탈퇴하지 않은 사용자)
	 */
	@Query("SELECT COUNT(u) FROM User u WHERE (u.withdrawYn != 'Y' OR u.withdrawYn IS NULL)")
	long countActiveUsers();

	/**
	 * 사용자 타입별 사용자 수 조회 (탈퇴자 제외)
	 */
	@Query("SELECT COUNT(u) FROM User u WHERE u.userTypeId.userTypeId = :userTypeId AND (u.withdrawYn != 'Y' OR u.withdrawYn IS NULL)")
	long countByUserTypeIdAndNotWithdrawn(@Param("userTypeId") Long userTypeId);

	/**
	 * 소셜 로그인 사용자 수 조회
	 */
	@Query("SELECT COUNT(u) FROM User u WHERE u.password = 'SOCIAL_LOGIN' AND (u.withdrawYn != 'Y' OR u.withdrawYn IS NULL)")
	long countSocialLoginUsers();


	/**
	 * 권한별 사용자 검색 (검색어 + 권한 필터 조합)
	 * @param keyword 검색 키워드
	 * @param userTypeId 사용자 타입 ID
	 * @param pageable 페이징 정보
	 * @return 검색된 사용자 페이지
	 */
	@Query("SELECT u FROM User u WHERE u.withdrawYn != 'Y' " +
			"AND u.userTypeId.userTypeId = :userTypeId " +
			"AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
			"OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
			"OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<User> searchUsersByRole(@Param("keyword") String keyword,
								 @Param("userTypeId") Long userTypeId,
								 Pageable pageable);


}
