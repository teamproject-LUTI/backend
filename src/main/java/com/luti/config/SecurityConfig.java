package com.luti.config;

import java.util.Arrays;
import java.util.List;

import com.amadeus.Amadeus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.luti.auth.security.JwtAuthenticationFilter;
import com.luti.auth.security.OAuth2AuthenticationFailureHandler;
import com.luti.auth.security.OAuth2AuthenticationSuccessHandler;
import com.luti.auth.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 설명: Spring Security 설정을 정의하는 클래스입니다.
 * 애플리케이션의 전반적인 보안 규칙, 인증 방식 (JWT, OAuth2), 권한 부여, 세션 관리 등을 구성합니다.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;

	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	/**
	 * 설명: Spring Security 필터 체인을 구성하는 Bean을 정의합니다.
	 * HTTP 요청에 대한 보안 규칙, CORS, 세션 관리, OAuth2 로그인, 로그아웃, 예외 처리 등을 설정합니다.
	 *
	 * @param http HttpSecurity 객체, HTTP 요청에 대한 보안 설정을 구성하는 데 사용됩니다.
	 * @return SecurityFilterChain 구성된 SecurityFilterChain 객체입니다.
	 * @throws Exception 보안 구성 중 발생할 수 있는 예외입니다.
	 * @author
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("SecurityFilterChain 설정 시작");

		return http
				// CSRF 비활성화 (JWT 사용으로 인해)
				.csrf(AbstractHttpConfigurer::disable)

				// CORS 설정 적용
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// 세션 관리 정책 설정 (JWT 사용 시 STATELESS)
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)

				// HTTP 요청에 대한 인가 규칙 설정
				.authorizeHttpRequests(auth -> {
					log.info("HTTP 요청 권한 설정");
					auth
							.requestMatchers(
									"/api/auth/refresh",       // 토큰 갱신
									"/oauth2/**",              // OAuth2 관련 엔드포인트
									"/login/oauth2/**",        // OAuth2 로그인 리다이렉션
									"/",                       // 루트 경로
									"/login",                  // 로그인 페이지
									"/signup",                 // 회원가입 페이지
									"/public/**",              // 공개 리소스
									"/health",                 // 상태 확인
									"/actuator/**",            // Spring Boot 액추에이터 엔드포인트
									"/error",
									"/favicon.ico" // 에러 페이지
							).permitAll()

							// 관리자 역할이 필요한 경로 설정
							.requestMatchers("/api/admin/**").hasRole("ADMIN")

							.requestMatchers(
									"/api/auth/validate",
									"/api/auth/me",
									"/api/auth/logout",
									"/api/auth/logout-all",
									"/api/auth/withdraw",      // 회원탈퇴 추가
									"/api/auth/restore",       // 계정복구 추가
									"/api/auth/withdraw/status", // 탈퇴 상태 확인 추가
									"/api/mypage/**",
									"/api/user/**",
									"/api/payments/"
							).authenticated()

							.anyRequest().authenticated();
				})

				// OAuth2 로그인 설정
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/login") // OAuth2 로그인 시작 페이지
						.authorizationEndpoint(authorization ->
								authorization.baseUri("/oauth2/authorization") // OAuth2 인증 요청을 시작할 기본 URI
						)
						.redirectionEndpoint(redirection ->
								redirection.baseUri("/login/oauth2/code/*") // OAuth2 공급자로부터 리다이렉션될 URI 패턴
						)
						.userInfoEndpoint(userInfo ->
								userInfo.userService(customOAuth2UserService)
						)
						.successHandler(oAuth2AuthenticationSuccessHandler)
						.failureHandler(oAuth2AuthenticationFailureHandler)
				)

				// 로그아웃 설정
				.logout(logout -> logout
						.logoutUrl("/api/auth/logout") // 로그아웃을 처리할 URL
						.logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉션될 URL
						.deleteCookies("accessToken", "refreshToken", "JSESSIONID") // 로그아웃 시 삭제할 쿠키
						.invalidateHttpSession(true) // HTTP 세션 무효화
						.clearAuthentication(true) // SecurityContextHolder의 인증 정보 삭제
				)

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

				// 예외 처리 설정
				.exceptionHandling(exceptions -> exceptions
						// 인증 실패 시 (401 Unauthorized)
						.authenticationEntryPoint((request, response, authException) -> {
							log.debug("인증 실패 - 경로: {}, 오류: {}", request.getRequestURI(), authException.getMessage());
							response.setStatus(401);
							response.setContentType("application/json;charset=UTF-8");
							response.getWriter().write(
									"{\"success\": false, \"error\": \"인증이 필요합니다.\"}"
							);
						})
						// 접근 거부 시 (403 Forbidden)
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							log.debug("접근 거부 - 경로: {}, 오류: {}", request.getRequestURI(),
									accessDeniedException.getMessage());
							response.setStatus(403);
							response.setContentType("application/json;charset=UTF-8");
							response.getWriter().write(
									"{\"success\": false, \"error\": \"접근 권한이 없습니다.\"}"
							);
						})
				)

				.build();
	}

	/**
	 * 설명: CORS(Cross-Origin Resource Sharing) 설정을 정의하는 Bean입니다.
	 * 특정 오리진, HTTP 메서드, 헤더, 자격 증명 등을 허용하도록 구성하여 교차 출처 요청을 처리합니다.
	 *
	 * @return CorsConfigurationSource 구성된 CorsConfigurationSource 객체입니다.
	 * @author
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		log.info("CORS 설정");

		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOriginPatterns(Arrays.asList(
				"http://localhost:3000",
				"http://localhost:8080",
				"http://localhost:3001"
		));

		// 허용할 HTTP 메서드 설정
		configuration.setAllowedMethods(Arrays.asList(
				"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
		));

		// 허용할 모든 헤더 설정
		configuration.setAllowedHeaders(List.of("*"));

		// 자격 증명(쿠키, Authorization 헤더 등)을 교차 출처 요청에 포함할지 여부
		configuration.setAllowCredentials(true);

		// 클라이언트가 접근할 수 있도록 노출할 응답 헤더 설정
		configuration.setExposedHeaders(Arrays.asList(
				"Authorization", "Content-Type", "X-Requested-With"
		));

		// Preflight 요청(사전 요청) 결과를 캐시할 시간 (초 단위)
		configuration.setMaxAge(3600L);

		// URL 기반 CORS 설정을 등록하는 소스
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용

		return source;
	}

	/**
	 * 설명: 비밀번호 암호화를 위한 BCryptPasswordEncoder Bean을 제공합니다.
	 * 사용자 비밀번호를 안전하게 저장하고 검증하는 데 사용됩니다.
	 *
	 * @return PasswordEncoder BCrypt 알고리즘을 사용하는 PasswordEncoder 구현체입니다.
	 * @author
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	@Bean
	public Amadeus amadeus(
			@Value("${amadeus.client-id}") String clientId,
			@Value("${amadeus.client-secret}") String clientSecret) {

		return Amadeus.builder(clientId, clientSecret)
				.build();
	}
	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}
}
