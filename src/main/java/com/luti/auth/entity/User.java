package com.luti.auth.entity;

import com.luti.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 정보를 관리하는 엔티티
 * 사용자의 기본 정보 저장 및 관리
 *
 * @author 박종호
 */
@Entity
@Table(name = "user_info")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Auditable {

	@Id
	@Column(name = "login_Id", length = 50)
	private String loginId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_type", referencedColumnName = "user_type_id")
	private UserType userType;

	@Column(name = "password", length = 100)
	private String password;

	@Column(name = "name", length = 20)
	private String name;

	@Column(name = "birthday", length = 15)
	private String birthday;

	@Column(name = "hp", length = 15)
	private String phoneNumber;

	@Column(name = "sex", length = 10)
	private String gender;

	@Column(name = "address")
	private Integer address;

	@Column(name = "nickname", length = 30)
	private String nickname;

	@Column(name = "file_nm", length = 200)
	private String profileFileName;

	@Column(name = "pypath", length = 200)
	private String profilePhysicalPath;

	@Column(name = "lopath", length = 200)
	private String profileLogicalPath;

	@Column(name = "extend", length = 5)
	private String profileExtension;

	@Column(name = "size")
	private Integer profileSize;

	@Column(name = "withdraw", length = 2)
	private String withdrawYn;

}
