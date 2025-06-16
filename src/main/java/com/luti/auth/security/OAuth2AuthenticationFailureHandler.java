package com.luti.auth.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 설명: OAuth2 소셜 로그인 실패 시 발생하는 이벤트를 처리하는 핸들러입니다.
 * Spring Security의 `SimpleUrlAuthenticationFailureHandler`를 확장하여,
 * 로그인 실패 시 프론트엔드 애플리케이션의 특정 에러 페이지로 사용자를 리다이렉션하고,
 * 실패 원인에 대한 사용자 친화적인 메시지와 에러 코드를 URL 쿼리 파라미터로 전달합니다.
 *
 * @author
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Value("${app.frontend.url:http://localhost:3000}")
	private String frontendUrl;

	@Value("${app.frontend.oauth2.failure-redirect-path:/auth/error}")
	private String failureRedirectPath;

	/**
	 * 설명: OAuth2 인증 실패 시 호출되는 콜백 메서드입니다.
	 * 발생한 `AuthenticationException`을 분석하여 에러 메시지와 코드를 생성하고,
	 * 이를 포함하여 프론트엔드의 지정된 에러 페이지로 클라이언트를 리다이렉션합니다.
	 *
	 * @param request 현재 HTTP 요청 객체.
	 * @param response 현재 HTTP 응답 객체.
	 * @param exception 발생한 인증 실패 예외 객체.
	 * @throws IOException 리다이렉션 중 입출력 오류가 발생할 경우.
	 * @throws ServletException 서블릿 관련 오류가 발생할 경우.
	 * @author
	 */
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		log.error("OAuth2 인증 실패: {}", exception.getMessage(), exception);

		// 1. 예외로부터 사용자 친화적 에러 메시지 생성
		String errorMessage = determineErrorMessage(exception);

		// 2. 예외로부터 에러 코드 생성
		String errorCode = determineErrorCode(exception);

		// 3. 프론트엔드 에러 페이지로 리다이렉션할 URL 구성
		String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + failureRedirectPath)
				.queryParam("error", errorCode)
				.queryParam("message", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
				.build().toUriString();

		log.info("OAuth2 인증 실패 - 리다이렉트 URL: {}", redirectUrl);

		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}

	/**
	 * 설명: 발생한 `AuthenticationException`의 타입과 내용을 기반으로 사용자에게 표시할 사용자 친화적인 에러 메시지를 결정합니다.
	 * OAuth2 관련 표준 에러 코드와 커스텀 예외 메시지를 처리하여 적절한 설명을 반환합니다.
	 *
	 * @param exception 처리할 인증 실패 예외 객체.
	 * @return String 사용자에게 보여줄 에러 메시지.
	 * @author
	 */
	private String determineErrorMessage(AuthenticationException exception) {
		if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
			String errorCode = oauth2Exception.getError().getErrorCode();

			return switch (errorCode) {
				case "invalid_request" -> "잘못된 요청입니다. 다시 시도해주세요.";
				case "unauthorized_client" -> "인증되지 않은 클라이언트입니다.";
				case "access_denied" -> "소셜 로그인이 거부되었습니다.";
				case "unsupported_response_type" -> "지원하지 않는 응답 형식입니다.";
				case "invalid_scope" -> "잘못된 권한 범위입니다.";
				case "server_error" -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
				case "temporarily_unavailable" -> "일시적으로 서비스를 사용할 수 없습니다.";
				default -> oauth2Exception.getMessage();
			};
		}

		String message = exception.getMessage();
		if (message != null) {
			if (message.contains("이미 해당 이메일로 가입된 계정이 있습니다")) {
				return "이미 해당 이메일로 가입된 계정이 있습니다. 기존 계정으로 로그인해주세요.";
			}
			if (message.contains("기본 사용자 타입을 찾을 수 없습니다")) {
				return "시스템 설정 오류입니다. 관리자에게 문의해주세요.";
			}
		}

		return "소셜 로그인 중 오류가 발생했습니다. 다시 시도해주세요.";
	}

	/**
	 * 설명: 발생한 `AuthenticationException`의 타입과 내용을 기반으로 특정 에러 코드를 결정합니다.
	 * OAuth2 관련 표준 에러 코드와 커스텀 예외 메시지에 대한 고유한 에러 코드를 반환합니다.
	 *
	 * @param exception 처리할 인증 실패 예외 객체.
	 * @return String 프론트엔드에서 에러를 식별하는 데 사용될 에러 코드.
	 * @author
	 */
	private String determineErrorCode(AuthenticationException exception) {
		if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
			return oauth2Exception.getError().getErrorCode();
		}

		String message = exception.getMessage();
		if (message != null) {
			if (message.contains("이미 해당 이메일로 가입된 계정이 있습니다")) {
				return "email_already_exists";
			}
			if (message.contains("기본 사용자 타입을 찾을 수 없습니다")) {
				return "system_configuration_error";
			}
		}

		return "oauth2_authentication_failed";
	}

}
