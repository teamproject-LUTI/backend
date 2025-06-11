package com.luti.auth.config;

import com.luti.auth.dto.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    //Spring Security의 인증 관리자 - 사용자의 인증을 관리
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    //유저의 비밀번호 암호화에 사용
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //Form 로그인 방식 사용하지 않음
                .formLogin((auth) -> auth.disable())
                //http basic 인증 방식 사용하지 않음
                .httpBasic((auth) -> auth.disable())
                //csrf 사용하지 않음 - JWT는 서버에 인증정보를 보관하지 않기 때문에 crsf 코드를 작성할 필요 없음
                .csrf(csrf -> csrf.disable())
                //세션 설정 - JWT를 사용하기 때문에 세션을 사용하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //경로별 인가 작업
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/").permitAll()
                        .anyRequest().authenticated())
                //Spring Security 필터 체인에 CORS를 적용
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); //허용할 도메인 (프론트엔드 주소)
                        configuration.setAllowedMethods(Collections.singletonList("*")); //모든 HTTP 메서드 허용 (GET, POST, PUT, 등)
                        configuration.setAllowCredentials(true); //인증정보(쿠키, 인증헤더 등) 포함 허용
                        configuration.setAllowedHeaders(Collections.singletonList("*")); //모든 요청 헤더 허용
                        configuration.setMaxAge(3600L); //preflight 요청의 캐시 유효 시간 (초 단위)
                        configuration.setExposedHeaders(Collections.singletonList("Authorization")); //응답에서 브라우저가 접근할 수 있는 헤더

                        return configuration;
                    }
                })));


        //필터 추가 (추후 반영)
        // http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
        // http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



}
