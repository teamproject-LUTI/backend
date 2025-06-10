package com.luti.luti.payment.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_info")
public class UserInfo {

    // 회원 로그인 ID (기본 키)
    @Id
    @Column(name = "loginID", length = 50, nullable = false)
    private String loginId;

    // 비밀번호 (암호화 저장 권장)
    @Column(name = "user_pw", length = 100)
    private String password;

    // 이름
    @Column(name = "name", length = 20)
    private String name;

    // 생년월일 (문자열로 저장)
    @Column(name = "birthday", length = 15)
    private String birthday;

    // 휴대폰 번호
    @Column(name = "hp", length = 15)
    private String hp;

    // 성별
    @Column(name = "sex", length = 10)
    private String sex;

    // 주소
    @Column(name = "address")
    private String address;

    // 가입일
    @Column(name = "create_date")
    private LocalDate createDate;

    // 닉네임
    @Column(name = "nickname", length = 30)
    private String nickname;

    // 프로필 파일명
    @Column(name = "file_nm", length = 200)
    private String fileName;

    // 프로필 파일 경로
    @Column(name = "filepath", length = 200)
    private String filepath;

    // 확장자
    @Column(name = "extend", length = 20)
    private String extend;

    // 파일 크기
    @Column(name = "size")
    private Integer size;

    // 탈퇴 여부 ("Y"/"N")
    @Column(name = "withdrawal", length = 2)
    private String withdrawal;

    // 기본 생성자, Getter/Setter 등 생략
}
