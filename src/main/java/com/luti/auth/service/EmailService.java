package com.luti.auth.service;

import com.luti.auth.dto.EmailRequestDto;
import com.luti.auth.repository.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

//이메일 인증 서비스
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final JavaMailSender mailSender;

    @Value("${spring.mail.verification-url}")
    private String verificationUrl;

    // 1. 이메일 중복여부 확인
    public void checkDuplicate(EmailRequestDto emailRequestDto) {
        if (userRepository.existsByEmail(emailRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }

    // 2. 이메일 전송 및 인증코드 반환
    public String sendEmail(EmailRequestDto emailRequestDto) throws MessagingException, UnsupportedEncodingException {
        //6자리 숫자 코드 생성
        String code = generateSecureCode();

        // 이메일 메시지 구성
        MimeMessage message = mailSender.createMimeMessage();
        message.setSubject("[LUTI] 회원가입 인증코드 안내", "utf-8");
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailRequestDto.getEmail()));

        // HTML 형식 본문 구성
        String body = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2>이메일 인증 코드</h2>"
                + "<p>아래 인증코드를 입력해주세요:</p>"
                + "<div style='font-size: 24px; font-weight: bold; color: #4CAF50; margin: 10px 0;'>"
                + code + "</div>"
                + "<p>인증코드는 5분간 유효합니다.</p>"
                + "<p style='margin-top: 20px;'>감사합니다.<br>LUTI 팀 드림</p>"
                + "</div>";

        message.setContent(body, "text/html; charset=utf-8");
        message.setFrom(new InternetAddress("noreply@luti.com", "LUTI 관리자"));

        mailSender.send(message);

        return code;
    }

    private String generateSecureCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

}
