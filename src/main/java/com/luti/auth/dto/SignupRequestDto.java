package com.luti.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private String phonePrefix;
    private String phoneNumber;
    private String gender;
    private String nickname;
    private String address;

    public String getFullBirthday() {
        return birthYear + birthMonth + birthDay;
    }

    public String getFullPhoneNumber() {
        return phonePrefix + phoneNumber;
    }
}
