package com.luti.auth.entity;

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
import lombok.Setter;

/**
 * 사용자 정보를 관리하는 엔티티
 * 기존 컬럼을 활용한 소셜 로그인 지원
 */
@Entity
@Table(name = "user_info")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_type")
	private UserType userTypeId;

	@Column(name = "email", length = 50)
	private String email;

	@Column(name = "password", length = 100)
	private String password; // 소셜 로그인 시 NULL 또는 "SOCIAL_LOGIN"

	@Column(name = "name", length = 20)
	private String name;

	@Column(name = "birthday", length = 15)
	private String birthday; // 소셜 로그인 제공자 저장 (google, kakao)

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
	private String profileLogicalPath; // 소셜 프로필 이미지 URL 저장

	@Column(name = "extend", length = 50)
	private String profileExtension; // 소셜 로그인 ID 저장

	@Column(name = "size")
	private Integer profileSize;

	@Column(name = "withdraw", length = 2)
	private String withdrawYn;

	@Builder
	private User(String email, String password, String name, String birthday,
			String phoneNumber, String gender, Integer address, String nickname,
			String profileFileName, String profilePhysicalPath, String profileLogicalPath,
			String profileExtension, Integer profileSize, String withdrawYn, UserType userTypeId) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.birthday = birthday;
		this.phoneNumber = phoneNumber;
		this.gender = gender;
		this.address = address;
		this.nickname = nickname;
		this.profileFileName = profileFileName;
		this.profilePhysicalPath = profilePhysicalPath;
		this.profileLogicalPath = profileLogicalPath;
		this.profileExtension = profileExtension;
		this.profileSize = profileSize;
		this.withdrawYn = withdrawYn != null ? withdrawYn : "N";
		this.userTypeId = userTypeId;
	}

	/**
	 * 소셜 로그인용 정적 팩토리 메서드
	 * 기존 컬럼 활용:
	 * - birthday: 소셜 제공자 (google, kakao)
	 * - profileLogicalPath: 프로필 이미지 URL
	 * - profileExtension: 소셜 ID
	 * - password: "SOCIAL_LOGIN"
	 */
	public static User createSocialUser(String email, String name, String profileImageUrl, UserType userType) {
		return User.builder()
				.email(email)
				.password("SOCIAL_LOGIN") // 소셜 로그인 구분자
				.name(name)
				.nickname(name)
				.profileLogicalPath(profileImageUrl) // 프로필 이미지 URL
				.withdrawYn("N")
				.userTypeId(userType)
				.build();
	}

	/**
	 * 일반 회원가입용 정적 팩토리 메서드
	 */
	public static User createRegularUser(String email, String password, String name,
			String phoneNumber, UserType userType) {
		return User.builder()
				.email(email)
				.password(password)
				.name(name)
				.phoneNumber(phoneNumber)
				.withdrawYn("N")
				.userTypeId(userType)
				.build();
	}

	/**
	 * 소셜 정보 업데이트
	 */
	public void updateSocialInfo(String name, String profileImageUrl) {
		if (name != null && !name.trim().isEmpty()) {
			this.name = name;
			if (this.nickname == null || this.nickname.equals(this.name)) {
				this.nickname = name;
			}
		}
		if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
			this.profileLogicalPath = profileImageUrl;
		}
	}

	/**
	 * 소셜 제공자 정보 설정
	 * birthday 컬럼을 소셜 제공자로 활용
	 * profileExtension 컬럼을 소셜 ID로 활용
	 */
	public void setSocialProvider(String provider, String socialId) {
		if (this.birthday == null || this.birthday.equals("google") || this.birthday.equals("kakao") || this.birthday.equals("naver")) {
			this.birthday = provider; // 소셜 제공자 정보만 저장
		}
		this.profileExtension = socialId; // 소셜 ID
	}

	/**
	 * 기본 사용자 타입 설정
	 */
	public void setDefaultUserType(UserType defaultUserType) {
		if (this.userTypeId == null) {
			this.userTypeId = defaultUserType;
		}
	}

	/**
	 * 소셜 로그인 사용자인지 확인
	 */
	public boolean isSocialUser() {
		return "SOCIAL_LOGIN".equals(this.password);
	}

	/**
	 * 소셜 제공자 반환
	 */
	public String getSocialProvider() {
		return isSocialUser() ? this.birthday : null;
	}

	/**
	 * 소셜 ID 반환
	 */
	public String getSocialId() {
		return isSocialUser() ? this.profileExtension : null;
	}

	/**
	 * 표시할 프로필 이미지 반환
	 */
	public String getDisplayProfileImage() {
		return this.profileLogicalPath;
	}

	/**
	 * 표시할 이름 반환
	 */
	public String getDisplayName() {
		return (nickname != null && !nickname.trim().isEmpty()) ? nickname : name;
	}

	/**
	 * 비밀번호 존재 여부 (일반 로그인 가능 여부)
	 */
	public boolean hasPassword() {
		return password != null && !password.trim().isEmpty() && !"SOCIAL_LOGIN".equals(password);
	}

}
