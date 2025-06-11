package com.luti.auth.service;

import com.luti.auth.dto.EmailRequestDto;
import com.luti.auth.repository.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

//이메일 인증 서비스
@Service
public class EmailService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public EmailService(UserRepository userRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    //1. 이메일 중복여부 확인
    public void checkDuplicate(EmailRequestDto emailRequestDto) {
        if (userRepository.existsByLoginId(emailRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }

    //2. 이메일 인증 요청
    public void sendEmail(EmailRequestDto emailRequestDto) throws MessagingException, UnsupportedEncodingException {
        //랜덤 토큰 생성
        String token = UUID.randomUUID().toString();

        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, emailRequestDto.getEmail()); //이메일 전송 대상
        message.setSubject("[LUTI] 회원가입 인증메일입니다."); //제목
        String body = "<div>"
                + "<h1> 안녕하세요. LUTI 입니다</h1>"
                + "<br>"
                + "<p>아래 링크를 클릭하면 이메일 인증이 완료됩니다.<p>"
                + "<a href='http://localhost:3000/auth/verify?token=" + token + "'>인증 링크</a>"
                + "</div>";
        message.setText(body, "utf-8", "html"); //내용, charset 타입, subtype
        message.setFrom(new InternetAddress("???", "Artify_Admin")); //이메일 발송자 확인후 수정
        mailSender.send(message); //메일 전송
    }

}
