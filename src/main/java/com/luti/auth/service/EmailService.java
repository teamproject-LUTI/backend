package com.luti.auth.service;

import com.luti.auth.dto.EmailVerificationRequestDto;
import com.luti.auth.enums.EmailCheckType;
import com.luti.auth.repository.UserRepository;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

/**
 * 이메일 인증 서비스 - 비동기 처리로 성능 개선
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final UserRepository userRepository;

	private final JavaMailSender mailSender;

	private final AsyncEmailSender asyncEmailSender; // 별도 서비스 주입

	@Value("${spring.mail.verification-url}")
	private String verificationUrl;

	/**
	 * 이메일 전송 및 인증코드 반환
	 * 검증은 동기로 처리하고, 실제 이메일 전송만 비동기로 처리합니다.
	 */
	public String sendEmail(EmailVerificationRequestDto requestDto) throws
			MessagingException,
			UnsupportedEncodingException {
		String email = requestDto.getEmail();
		String name = requestDto.getName();
		EmailCheckType checkType = requestDto.getCheckType();

		// 1. 이메일 검증 (동기 처리 - 즉시 결과 확인 필요)
		validateEmailRequest(email, name, checkType);

		// 2. 6자리 숫자 코드 생성
		String code = generateSecureCode();

		// 3. 이메일 메시지 구성
		MimeMessage message = createEmailMessage(email, code);

		// 4. 별도 서비스를 통한 비동기 이메일 전송 (self-invocation 문제 해결)
		asyncEmailSender.sendEmailAsync(message, email);

		log.info("이메일 인증 코드 전송 요청 완료 - 수신자: {}, 비동기 처리로 즉시 응답", email);

		return code;
	}

	/**
	 * 임시 비밀번호 전송
	 */
	public void sendTempPassword(String email, String tempPassword) throws
			MessagingException,
			UnsupportedEncodingException {
		MimeMessage message = createTempPasswordMessage(email, tempPassword);

		// 별도 서비스를 통한 비동기 임시 비밀번호 전송
		asyncEmailSender.sendEmailAsync(message, email);

		log.info("임시 비밀번호 전송 요청 완료 - 수신자: {}, 비동기 처리로 즉시 응답", email);
	}

	/**
	 * 이메일 요청 검증 (기존 로직 분리)
	 */
	private void validateEmailRequest(String email, String name, EmailCheckType checkType) {
		switch (checkType) {
			case DUPLICATE -> {
				if (userRepository.existsByEmail(email)) {
					throw new IllegalArgumentException("이미 가입된 이메일입니다.");
				}
			}
			case MATCH_NAME -> {
				if (name == null || name.isBlank()) {
					throw new IllegalArgumentException("이름은 필수 입력값입니다.");
				}
				boolean exists = userRepository.findByNameAndEmail(name, email).isPresent();
				if (!exists) {
					throw new IllegalArgumentException("이름과 이메일이 일치하는 회원이 존재하지 않습니다.");
				}
			}
		}
	}

	/**
	 * 인증 코드 이메일 메시지 생성
	 */
	private MimeMessage createEmailMessage(String email, String code) throws
			MessagingException,
			UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		message.setSubject("[LUTI] 이메일 인증코드 안내", "utf-8");
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));

		String body = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
					  + "<h2>이메일 인증 코드</h2>"
					  + "<p>아래 인증코드를 입력해주세요:</p>"
					  + "<div style='font-size: 24px; font-weight: bold; color: #F76B59; margin: 10px 0;'>"
					  + code + "</div>"
					  + "<p>인증코드는 5분간 유효합니다.</p>"
					  + "<p style='margin-top: 20px;'>감사합니다.<br>LUTI 팀 드림</p>"
					  + "</div>";

		message.setContent(body, "text/html; charset=utf-8");
		message.setFrom(new InternetAddress("noreply@luti.com", "LUTI 관리자"));

		return message;
	}

	/**
	 * 임시 비밀번호 이메일 메시지 생성
	 */
	private MimeMessage createTempPasswordMessage(String email, String tempPassword) throws
			MessagingException,
			UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
		message.setSubject("[LUTI] 임시 비밀번호 발급 안내", "utf-8");

		String body = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
					  + "<h2>임시 비밀번호 발급</h2>"
					  + "<p>아래 임시 비밀번호로 로그인 후, 반드시 비밀번호를 변경해주세요.</p>"
					  + "<div style='font-size: 24px; font-weight: bold; color: #F76B59; margin: 10px 0;'>"
					  + tempPassword + "</div>"
					  + "<p style='margin-top: 20px;'>감사합니다.<br>LUTI 팀 드림</p>"
					  + "</div>";

		message.setContent(body, "text/html; charset=utf-8");
		message.setFrom(new InternetAddress("noreply@luti.com", "LUTI 관리자"));

		return message;
	}

	/**
	 * 세션에 인증 정보 저장
	 */
	public void saveVerificationSession(HttpServletRequest request, String authCode, String email) {
		HttpSession session = request.getSession();
		session.setAttribute("authCode", authCode);
		session.setAttribute("authEmail", email);
		session.setMaxInactiveInterval(300); //5분 유효
	}

	/**
	 * 보안 인증 코드 생성 (6자리)
	 */
	private String generateSecureCode() {
		SecureRandom random = new SecureRandom();
		int code = random.nextInt(900000) + 100000;
		return String.valueOf(code);
	}

}
