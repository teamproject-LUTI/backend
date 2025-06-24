package com.luti.mypage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.mypage.dto.RouteDto;
import com.luti.mypage.entity.Route;
import com.luti.mypage.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 현재 로그인한 사용자 조회
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // JWT 토큰 기반 인증인지 확인
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            Long userId = jwtToken.getCurrentUserId();
            String email = jwtToken.getCurrentUserEmail();

            log.debug("JWT Authentication - User ID: {}, Email: {}", userId, email);

            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));
        } else {
            // OAuth2 또는 다른 방식의 인증인 경우 이메일로 조회
            String email = authentication.getName();
            log.debug("Non-JWT Authentication - Email: {}", email);

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. email: " + email));
        }
    }

    /**
     * 여행 루트를 즐겨찾기에 저장
     */
    @Transactional
    public Route saveRoute(String routeTitle, Map<String, Object> routeData) {
        User user = getCurrentUser();

        try {
            // 루트 데이터를 JSON으로 변환
            String routeContentJson = objectMapper.writeValueAsString(routeData);

            // Route 엔티티 생성 및 저장 - 빌더 형식
            Route route = Route.builder()
                    .userId(user)
                    .routeTitle(routeTitle)
                    .routeContent(routeContentJson)
                    .build();

            // 저장
            Route savedRoute = routeRepository.save(route);

            log.info("사용자 {}(ID: {})의 여행 루트 '{}' 저장 완료", user.getEmail(), user.getUserId(), routeTitle);
            return savedRoute;

        } catch (Exception e) {
            log.error("여행 루트 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("여행 루트 저장에 실패했습니다.", e);
        }
    }

    /**
     * 사용자의 모든 여행 루트 조회
     */
    public List<RouteDto.Response> getUserRoutes() {
        User user = getCurrentUser();
        List<Route> routes = routeRepository.findByUserIdOrderByRouteIdDesc(user);

        return routes.stream()
                .map(route -> RouteDto.Response.builder()
                        .routeId(route.getRouteId())
                        .routeTitle(route.getRouteTitle())
                        .routeContent(route.getRouteContent())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 루트 삭제
     */
    @Transactional
    public void deleteRoute(Long routeId) {
        User user = getCurrentUser();

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("여행 루트를 찾을 수 없습니다."));

        // 본인의 루트인지 확인
        if (!route.getUserId().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인의 여행 루트만 삭제할 수 있습니다.");
        }

        routeRepository.delete(route);
        log.info("사용자 {}(ID: {})의 여행 루트 {} 삭제 완료", user.getEmail(), user.getUserId(), routeId);
    }

    /**
     * 여행 루트 상세 조회 (JSON 파싱)
     */
    public RouteDto.DetailResponse getRouteDetail(Long routeId) {
        User user = getCurrentUser();

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("여행 루트를 찾을 수 없습니다."));

        // 본인의 루트인지 확인
        if (!route.getUserId().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인의 여행 루트만 조회할 수 있습니다.");
        }

        try {
            // JSON 문자열을 Map으로 변환
            Map<String, Object> routeData = objectMapper.readValue(route.getRouteContent(), Map.class);

            return RouteDto.DetailResponse.builder()
                    .routeId(route.getRouteId())
                    .routeTitle(route.getRouteTitle())
                    .routeData(routeData)
                    .build();

        } catch (Exception e) {
            log.error("여행 루트 데이터 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("여행 루트 데이터를 읽을 수 없습니다.", e);
        }
    }
}