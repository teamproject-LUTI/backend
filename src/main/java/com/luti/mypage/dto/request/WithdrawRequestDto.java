package com.luti.mypage.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 회원탈퇴 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "password")
public class WithdrawRequestDto {

	/**
	 * 비밀번호 (일반 로그인 사용자만 필요)
	 * 소셜 로그인 사용자는 null 또는 빈 문자열
	 */
	private String password;

	/**
	 * 탈퇴 사유 (선택사항)
	 */
	private String reason;

}
