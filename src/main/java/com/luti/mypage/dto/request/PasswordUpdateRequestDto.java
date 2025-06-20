package com.luti.mypage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 비밀번호 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"currentPassword", "newPassword", "confirmPassword"}) // 보안상 비밀번호 로그 제외
public class PasswordUpdateRequestDto {

	/**
	 * 현재 비밀번호 (검증용)
	 */
	@NotBlank(message = "현재 비밀번호를 입력해주세요.")
	private String currentPassword;

	/**
	 * 새 비밀번호
	 */
	@NotBlank(message = "새 비밀번호를 입력해주세요.")
	@Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요.")
	private String newPassword;

	/**
	 * 새 비밀번호 확인
	 */
	@NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
	private String confirmPassword;

}
