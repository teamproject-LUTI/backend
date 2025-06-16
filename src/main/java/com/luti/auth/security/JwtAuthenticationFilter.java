package com.luti.auth.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 설명: JWT(JSON Web Token) 기반 인증을 처리하는 Spring Security 필터입니다.
 * 모든 HTTP 요청에 대해 Access Token을 검증하고, 유효한 경우 사용자 정보를 추출하여
 * Spring Security 컨텍스트에 인증 객체를 설정함으로써 후속 보안 처리를 가능하게 합니다.
 *
 * @author
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	private final UserRepository userRepository;

	/**
	 * 설명: HTTP 요청이 들어올 때마다 실행되는 필터의 핵심 로직입니다.
	 * 쿠키에서 Access Token을 추출하고, 유효성을 검증한 후, 사용자 정보를 바탕으로 인증 객체를 생성하여
	 * SecurityContextHolder에 설정합니다.
	 *
	 * @param request HttpServletRequest 객체, 현재 HTTP 요청 정보를 포함합니다.
	 * @param response HttpServletResponse 객체, 현재 HTTP 응답 정보를 포함합니다.
	 * @param filterChain FilterChain 객체, 다음 필터로 요청을 전달하는 데 사용됩니다.
	 * @throws ServletException 필터 처리 중 서블릿 관련 오류가 발생할 경우.
	 * @throws IOException 필터 처리 중 입출력 오류가 발생할 경우.
	 * @author
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String requestPath = request.getRequestURI();
		log.debug("JWT 필터 실행 - 경로: {}", requestPath);

		// 1. 쿠키에서 Access Token 추출
		String token = extractTokenFromCookie(request, "accessToken");

		log.debug("추출된 토큰 존재 여부: {}", token != null);

		try {
			// 2. 토큰 존재 및 유효성 검증
			if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
				log.debug("토큰 유효성 검증 성공");

				// 3. 토큰 타입이 ACCESS인지 확인
				String tokenType = jwtUtil.getTokenType(token);
				if (!"ACCESS".equals(tokenType)) {
					log.warn("유효하지 않은 토큰 타입: {}", tokenType);
					filterChain.doFilter(request, response);
					return;
				}

				// 4. 토큰에서 사용자 정보(클레임) 추출
				Claims claims = jwtUtil.getClaimsFromToken(token);
				Long userId = Long.parseLong(claims.getSubject());

				// *** 5. 사용자 존재 여부 및 탈퇴 상태 확인 (새로 추가) ***
				User user = userRepository.findById(userId).orElse(null);
				if (user == null) {
					log.warn("삭제된 사용자의 토큰 사용 시도 - 사용자 ID: {}", userId);
					sendDeletedUserResponse(response, "삭제된 계정입니다. 다시 로그인해주세요.");
					return;
				}

				// 탈퇴한 사용자인 경우 특별 응답 (복구 가능)
				if ("Y".equals(user.getWithdrawYn())) {
					log.info("탈퇴한 사용자 로그인 시도 감지 - 사용자 ID: {}", userId);
					sendWithdrawResponse(response, "탈퇴한 계정입니다. 복구하시겠습니까?");
					return;
				}

				// 6. 나머지 토큰 정보 추출
				String email = claims.get("email", String.class);
				String name = claims.get("name", String.class);
				String nickname = claims.get("nickname", String.class);
				String profileImageUrl = claims.get("profileImageUrl", String.class);
				String provider = claims.get("provider", String.class);
				Long userTypeId = claims.get("userTypeId", Long.class);

				// 7. 사용자 타입에 따른 역할 결정
				String role = determineRole(userTypeId);

				// 8. Spring Security 인증 객체 생성
				JwtAuthenticationToken authentication = new JwtAuthenticationToken(
						userId,
						email,
						name,
						nickname,
						profileImageUrl,
						userTypeId,
						provider,
						Collections.singletonList(new SimpleGrantedAuthority(role))
				);

				// 9. 인증 상세 정보 설정 및 SecurityContext 설정
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);

				log.debug("JWT 인증 성공 - SecurityContext에 인증 정보 설정 완료");

			} else if (StringUtils.hasText(token)) {
				log.warn("유효하지 않은 JWT 토큰");
			} else {
				log.debug("JWT 토큰이 없음 - 익명 사용자로 처리");
			}

		} catch (Exception e) {
			log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage(), e);
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * 설명: HttpServletRequest의 쿠키에서 지정된 이름의 토큰 값을 추출합니다.
	 *
	 * @param request HttpServletRequest 객체.
	 * @param cookieName 추출할 토큰이 담긴 쿠키의 이름 (예: "accessToken").
	 * @return String 추출된 토큰 값 또는 해당 쿠키가 없는 경우 null.
	 * @author
	 */
	private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookieName.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		log.debug("{} 쿠키를 찾을 수 없음", cookieName);
		return null;
	}

	/**
	 * 설명: 사용자 타입 ID에 따라 Spring Security 역할을 결정합니다.
	 * userTypeId가 1이면 "ROLE_USER", 2이면 "ROLE_ADMIN"을 반환하며, 그 외의 경우 기본값인 "ROLE_USER"를 반환합니다.
	 *
	 * @param userTypeId 사용자의 타입 ID.
	 * @return String 해당 userTypeId에 매핑되는 Spring Security 역할 문자열 (예: "ROLE_USER").
	 * @author
	 */
	private String determineRole(Long userTypeId) {
		if (userTypeId == null) {
			return "ROLE_USER"; // userTypeId가 없는 경우 기본 역할 부여
		}

		return switch (userTypeId.intValue()) {
			case 1 -> "ROLE_USER";
			case 2 -> "ROLE_ADMIN";
			default -> "ROLE_USER"; // 정의되지 않은 경우 기본 역할 부여
		};
	}

	/**
	 * 설명: 특정 요청 경로에 대해 이 JWT 필터를 건너뛸지 여부를 결정합니다.
	 * 이 메서드가 `true`를 반환하면 해당 요청은 JWT 필터의 `doFilterInternal` 메서드를 거치지 않고 바로 다음 필터로 넘어갑니다.
	 * 주로 인증이 필요 없는 공개 경로, OAuth2 관련 경로, 헬스 체크 경로 등에 사용됩니다.
	 *
	 * @param request HttpServletRequest 객체.
	 * @return boolean 해당 요청에 대해 JWT 필터를 건너뛰려면 `true`, 그렇지 않으면 `false`.
	 * @throws ServletException 필터 처리 중 서블릿 관련 오류가 발생할 경우.
	 * @author
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();

		// JWT 검증이 명시적으로 필요한 특정 API 경로 (이들은 필터를 스킵하지 않음)
		// 이 경로들은 Access Token이 없으면 401 응답을 받아야 함
		if (path.equals("/api/auth/validate") ||
			path.equals("/api/auth/me") ||
			path.equals("/api/auth/logout") ||
			path.equals("/api/auth/logout-all")) {
			log.debug("JWT 필터 실행 필요 - 경로: {}", path);
			return false; // 필터 실행 (스킵하지 않음)
		}

		// JWT 필터를 스킵할 경로들 (인증이 필요 없거나 다른 방식으로 처리되는 경로)
		boolean shouldNotFilter = path.equals("/api/auth/refresh") ||
								  path.startsWith("/oauth2/") ||
								  path.startsWith("/login/oauth2/") ||
								  path.equals("/") ||
								  path.startsWith("/public/") ||
								  path.startsWith("/health") ||
								  path.startsWith("/actuator/");

		if (shouldNotFilter) {
			log.debug("JWT 필터 스킵 - 경로: {}", path);
		}

		return shouldNotFilter;
	}

	/**
	 * 삭제된 사용자에 대한 특별 응답 전송
	 *
	 * @param response HTTP 응답 객체
	 * @param message 응답 메시지
	 */
	private void sendDeletedUserResponse(HttpServletResponse response, String message) throws IOException {
		// 쿠키 삭제 (삭제된 사용자의 토큰 정리)
		clearAccessTokenCookie(response);
		clearRefreshTokenCookie(response);
		clearJSessionIdCookie(response);

		response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 상태코드
		response.setContentType("application/json;charset=UTF-8");

		String jsonResponse = String.format(
				"{\"success\": false, \"error\": \"%s\", \"errorCode\": \"USER_DELETED\", \"needsLogin\": true}",
				message
		);

		response.getWriter().write(jsonResponse);
	}

	/**
	 * 탈퇴한 사용자에 대한 특별 응답 전송
	 *
	 * @param response HTTP 응답 객체
	 * @param message 응답 메시지
	 */
	private void sendWithdrawResponse(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value()); // 403 상태코드
		response.setContentType("application/json;charset=UTF-8");

		String jsonResponse = String.format(
				"{\"success\": false, \"error\": \"%s\", \"errorCode\": \"ACCOUNT_WITHDRAWN\", \"needsRestore\": true}",
				message
		);

		response.getWriter().write(jsonResponse);
	}

	/**
	 * Access Token 쿠키 삭제
	 */
	private void clearAccessTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("accessToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // 즉시 만료
		response.addCookie(cookie);
	}

	/**
	 * Refresh Token 쿠키 삭제
	 */
	private void clearRefreshTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("refreshToken", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // 즉시 만료
		response.addCookie(cookie);
	}

	/**
	 * JSESSIONID 쿠키 삭제
	 */
	private void clearJSessionIdCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("JSESSIONID", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // 즉시 만료
		response.addCookie(cookie);
	}

}
