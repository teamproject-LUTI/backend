package com.luti.mypage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.mypage.dto.TravelRecordDto;
import com.luti.mypage.entity.TravelRecord;
import com.luti.mypage.repository.TravelRecordRepository;
import com.luti.payment.repository.PaymentListRepository;

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
public class TravelRecordService {

    private final TravelRecordRepository travelRecordRepository;
    private final UserRepository userRepository;
    private final PaymentListRepository paymentListRepository;
    private final ObjectMapper objectMapper;

    /**
     * 현재 로그인한 사용자 조회 (RouteService와 동일한 패턴)
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
     * 여행 기록 저장 (결제 정보와 함께) - Route와 동일한 패턴
     */
    @Transactional
    public TravelRecord saveTravelRecord(String travelTitle, Map<String, Object> travelData, Long paymentId, Integer paymentCd) {
        User user = getCurrentUser();

        try {
            // 여행 데이터를 JSON으로 변환
            String travelContentJson = objectMapper.writeValueAsString(travelData);

            // ✅ Route와 동일한 패턴으로 User 객체 저장
            TravelRecord travelRecord = TravelRecord.builder()
                    .userId(user) // ✅ User 객체 저장 (Route와 동일)
                    .paymentId(paymentId)
                    .paymentCd(paymentCd)
                    .travelTitle(travelTitle)
                    .travelContent(travelContentJson)
                    .build();

            TravelRecord savedRecord = travelRecordRepository.save(travelRecord);

            log.info("사용자 {}(ID: {})의 여행 기록 '{}' 저장 완료",
                    user.getEmail(), user.getUserId(), travelTitle);
            return savedRecord;

        } catch (Exception e) {
            log.error("여행 기록 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("여행 기록 저장에 실패했습니다.", e);
        }
    }

    /**
     * 여행 기록 저장 (간단 버전 - 결제 정보 없이)
     */
    @Transactional
    public TravelRecord saveTravelRecord(String travelTitle, Map<String, Object> travelData) {
        return saveTravelRecord(travelTitle, travelData, null, null);
    }

    /**
     * 현재 사용자의 모든 여행 기록 조회 - Route와 동일한 패턴
     */
    public List<TravelRecordDto.Response> getUserTravelRecords() {
        User user = getCurrentUser();
        // ✅ User 객체로 조회 (Route와 동일)
        List<TravelRecord> records = travelRecordRepository.findByUserIdOrderByTravelRecordIdDesc(user);

        return records.stream()
                .map(record -> TravelRecordDto.Response.builder()
                        .travelRecordId(record.getTravelRecordId())
                        .travelTitle(record.getTravelTitle())
                        .travelContent(record.getTravelContent())
                        .paymentId(record.getPaymentId())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 기록 상세 조회 (JSON 파싱) - Route와 동일한 패턴
     */
    public TravelRecordDto.DetailResponse getTravelRecordDetail(Long travelRecordId) {
        User user = getCurrentUser();

        // ✅ User 객체로 조회 (Route와 동일)
        TravelRecord record = travelRecordRepository.findByTravelRecordIdAndUserId(travelRecordId, user)
                .orElseThrow(() -> new RuntimeException("여행 기록을 찾을 수 없습니다."));

        try {
            // JSON 문자열을 Map으로 변환
            Map<String, Object> travelData = objectMapper.readValue(record.getTravelContent(), Map.class);

            return TravelRecordDto.DetailResponse.builder()
                    .travelRecordId(record.getTravelRecordId())
                    .travelTitle(record.getTravelTitle())
                    .travelData(travelData)
                    .paymentId(record.getPaymentId())
                    .build();

        } catch (Exception e) {
            log.error("여행 기록 데이터 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("여행 기록 데이터를 읽을 수 없습니다.", e);
        }
    }

    /**
     * 여행 기록 통계 조회 - Route와 동일한 패턴
     */
    public TravelRecordDto.StatsResponse getTravelStats() {
        User user = getCurrentUser();
        // ✅ User 객체로 조회 (Route와 동일)
        long totalRecords = travelRecordRepository.countByUserId(user);

        return TravelRecordDto.StatsResponse.builder()
                .totalRecords(totalRecords)
                .mostVisitedCity("통계 준비중") // 추후 구현 가능
                .build();
    }

    /**
     * 결제 ID로 기존 여행 기록 확인
     */
    public boolean existsByPaymentId(Long paymentId) {
        return travelRecordRepository.findByPaymentId(paymentId).isPresent();
    }

    /**
     * 결제 완료 후 여행 기록 자동 생성 (PaymentService에서 호출)
     */
    @Transactional
    public TravelRecord createTravelRecordFromPayment(Long userId, Long paymentId, Integer paymentCd,
                                                      String hotelName, String location,
                                                      String checkInDate, String checkOutDate, Integer totalPrice) {
        try {
            // ✅ userId로 User 객체 조회 (외부 호출용)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));

            // 여행 기록 데이터 구성
            Map<String, Object> travelData = Map.of(
                    "searchInfo", Map.of(
                            "cityCode", location,
                            "checkInDate", checkInDate,
                            "checkOutDate", checkOutDate,
                            "adults", 1
                    ),
                    "selectedPackage", Map.of(
                            "title", hotelName + " 여행",
                            "theme", "호텔",
                            "totalPrice", totalPrice,
                            "currency", "KRW",
                            "hotel", Map.of(
                                    "name", hotelName,
                                    "location", location,
                                    "category", "호텔"
                            ),
                            "completedAt", java.time.LocalDateTime.now().toString()
                    )
            );

            // ✅ User 객체를 사용하여 여행 기록 저장
            String travelContentJson = objectMapper.writeValueAsString(travelData);

            TravelRecord travelRecord = TravelRecord.builder()
                    .userId(user) // ✅ User 객체 저장
                    .paymentId(paymentId)
                    .paymentCd(paymentCd)
                    .travelTitle(hotelName + " 여행")
                    .travelContent(travelContentJson)
                    .build();

            return travelRecordRepository.save(travelRecord);

        } catch (Exception e) {
            log.error("결제 기반 여행 기록 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("여행 기록 생성에 실패했습니다.", e);
        }
    }
}