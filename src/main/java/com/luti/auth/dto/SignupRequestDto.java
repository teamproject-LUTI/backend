package com.luti.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "연도는 필수입니다.")
    private String birthYear;

    @NotBlank(message = "월은 필수입니다.")
    private String birthMonth;

    @NotBlank(message = "일은 필수입니다.")
    private String birthDay;

    @NotBlank(message = "전화번호 앞자리는 필수입니다.")
    private String phonePrefix;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "\\d{8}", message = "전화번호는 숫자 8자리여야 합니다.")
    private String phoneNumber;

    @NotBlank(message = "성별을 선택해주세요.")
    private String gender;

    @NotBlank(message = "별명은 필수입니다.")
    private String nickname;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    public String getFullBirthday() {
        return birthYear + birthMonth + birthDay;
    }

    public String getFullPhoneNumber() {
        return phonePrefix + phoneNumber;
    }
}
