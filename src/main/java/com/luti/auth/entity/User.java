package com.luti.auth.entity;

import com.luti.audit.Auditable;

import com.luti.auth.dto.SignupRequestDto;
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
	private String birthday;

	@Column(name = "hp", length = 15)
	private String phoneNumber;

	@Column(name = "sex", length = 10)
	private String gender;

	@Column(name = "address")
	private String address;

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

	@Column(name = "provider", length = 20)
	private String provider = "LOCAL";

	@Builder
	private User(String email, String password, String name, String birthday,
			String phoneNumber, String gender, String address, String nickname,
			String profileFileName, String profilePhysicalPath, String profileLogicalPath,
			String profileExtension, Integer profileSize, String withdrawYn, UserType userTypeId, String provider) {
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
		this.provider = provider != null ? provider : "LOCAL";
	}

	/**
	 * 소셜 로그인용 정적 팩토리 메서드 (Google, Kakao 공통)
	 * - profileLogicalPath: 프로필 이미지 URL
	 * - profileExtension: 소셜 ID
	 * - password: "SOCIAL_LOGIN"
	 * - provider: "GOOGLE", "KAKAO" 등
	 */
	public static User createSocialUser(String email, String name, String nickname,
			String birthday, String gender, String profileImageUrl, UserType userType) {
		return User.builder()
				.email(email)
				.password("SOCIAL_LOGIN") // 소셜 로그인 구분자
				.name(name)
				.nickname(nickname)
				.birthday(birthday) // 카카오의 경우 생년월일 포함
				.gender(gender)     // 카카오의 경우 성별 포함
				.profileLogicalPath(profileImageUrl)
				.provider("LOCAL")
				.withdrawYn("N")
				.userTypeId(userType)
				.build();
	}

	/**
	 * 일반 회원가입용 정적 팩토리 메서드
	 */
	public static User createRegularUser(SignupRequestDto dto, String encodedPassword, UserType userType) {
		return User.builder()
				.email(dto.getEmail())
				.password(encodedPassword)
				.name(dto.getName())
				.birthday(dto.getFullBirthday())
				.phoneNumber(dto.getFullPhoneNumber())
				.gender(dto.getGender())
				.address(dto.getAddress())
				.nickname(dto.getNickname())
				.provider("LOCAL")
				.withdrawYn("N")
				.userTypeId(userType)
				.build();
	}

	/**
	 * 소셜 정보 업데이트 (Google, Kakao 공통)
	 */
	public void updateSocialInfo(String name, String nickname, String profileImageUrl,
			String birthday, String gender) {
		if (name != null && !name.trim().isEmpty()) {
			this.name = name;
		}

		if (nickname != null && !nickname.trim().isEmpty()) {
			this.nickname = nickname;
		}

		if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
			this.profileLogicalPath = profileImageUrl;
		}

		// 카카오 전용: 생년월일 업데이트 (기존에 없는 경우)
		if (birthday != null && (this.birthday == null || this.birthday.trim().isEmpty())) {
			this.birthday = birthday;
		}

		// 카카오 전용: 성별 업데이트 (기존에 없는 경우)
		if (gender != null && (this.gender == null || this.gender.trim().isEmpty())) {
			this.gender = gender;
		}
	}
	/**
	 * 소셜 제공자 정보 설정
	 * provider 컬럼에 소셜 제공자 저장
	 * profileExtension 컬럼에 소셜 ID 저장
	 */
	public void setSocialProvider(String provider, String socialId) {
		this.provider = provider.toUpperCase(); // GOOGLE, KAKAO, NAVER
		this.profileExtension = socialId;
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
		return !"LOCAL".equals(this.provider);
	}

	/**
	 * 소셜 제공자 반환
	 */
	public String getSocialProvider() {
		return isSocialUser() ? this.provider : null;
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

	/**
	 * provider 필드 직접 반환 (JWT 토큰 생성용)
	 */
	public String getProvider() {
		return this.provider;
	}
}
