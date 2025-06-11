package com.luti.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 타입 정보를 관리하는 엔티티
 * 역할 구분을 위해 타입 정보를 저장
 *
 * @author 박종호
 */
@Entity
@Table(name = "user_type")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserType {

	@Id
	@Column(name = "user_type_id", length = 1)
	private String userTypeId;

	@Column(name = "type_name", length = 50, nullable = false)
	private String typeName;

	@Column(name = "description", length = 200)
	private String description;

}
