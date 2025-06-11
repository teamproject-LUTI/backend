package com.luti.auth.dto;

import lombok.Getter;

//클라이언트가 서버로 회원가입 정보 데이터를 보내는 객체
@Getter
public class JoinDto {
    private String loginId;
    private String password;
    private String name;
    private String birthday;
    private String phoneNumber;
    private String gender;
    private Integer address;
    private String nickname;
}
