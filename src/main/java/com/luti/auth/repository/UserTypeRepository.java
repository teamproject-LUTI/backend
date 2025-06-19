package com.luti.auth.repository;

import java.util.Optional;

import com.luti.auth.enums.UserTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import com.luti.auth.entity.UserType;

/**
 * 설명: UserType 엔티티에 대한 데이터베이스 접근을 위한 Spring Data JPA 리포지토리 인터페이스입니다.
 * 사용자 유형 정보(예: 일반 사용자, 관리자 등)를 조회하고 관리하는 기능을 제공합니다.
 *
 * @author
 */
public interface UserTypeRepository extends JpaRepository<UserType, Long> {

	/**
	 * 설명: 주어진 타입명(typeName)과 일치하는 UserType 엔티티를 조회합니다.
	 *
	 * @param typeName 조회할 사용자 유형의 이름 (예: "USER", "ADMIN").
	 * @return Optional<UserType> 해당 타입명을 가진 UserType 엔티티 (존재하지 않으면 Optional.empty()).
	 * @author
	 */
	Optional<UserType> findByTypeName(String typeName);

	/**
	 * 설명: 애플리케이션의 기본 사용자 타입(ID가 1인 UserType)을 조회합니다.
	 * 이 메서드는 인터페이스에 직접 구현되어 있으며, findById(1L)을 호출하여 기본 사용자 타입을 빠르게 가져옵니다.
	 *
	 * @return Optional<UserType> ID가 1인 UserType 엔티티 (존재하지 않으면 Optional.empty()).
	 * @author
	 */
	default Optional<UserType> findDefaultUserType() {
		return findById(1L);
	}
	/**
	 * 설명: 애플리케이션의 관리자 사용자 타입(ID가 2인 UserType)을 조회합니다.
	 * 이 메서드는 인터페이스에 직접 구현되어 있으며, findById(2L)을 호출하여 관리자 사용자 타입을 빠르게 가져옵니다.
	 *
	 * @return Optional<UserType> ID가 2인 UserType 엔티티 (존재하지 않으면 Optional.empty()).
	 * @author
	 */
	default Optional<UserType> findAdminUserType() {
		return findById(UserTypeEnum.ADMIN.getId());
	}
}
