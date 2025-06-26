package com.luti.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 비동기 이메일 전송 전용 서비스
 * self-invocation 문제 해결을 위해 별도 클래스로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncEmailSender {

	private final JavaMailSender mailSender;

	/**
	 * 비동기 이메일 전송 메서드
	 * @Async 어노테이션으로 별도 스레드에서 실행
	 */
	@Async("emailTaskExecutor")
	public void sendEmailAsync(MimeMessage message, String recipient) {
		try {
			long startTime = System.currentTimeMillis();
			log.debug("비동기 이메일 전송 시작 - 수신자: {}", recipient);

			mailSender.send(message);

			long endTime = System.currentTimeMillis();
			log.info("비동기 이메일 전송 완료 - 수신자: {}, 소요시간: {}ms", recipient, (endTime - startTime));

		} catch (Exception e) {
			log.error("비동기 이메일 전송 실패 - 수신자: {}, 오류: {}", recipient, e.getMessage(), e);
		}
	}
}
