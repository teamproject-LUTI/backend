package com.luti.payment.service;

import com.luti.payment.dto.PaymentListRequestDTO;
import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.entity.PaymentList;
import com.luti.payment.entity.PaymentMethod;
import com.luti.payment.repository.PaymentListRepository;
import com.luti.payment.repository.PaymentMethodRepository;
import com.luti.payment.dto.PaymentWithReservationDTO;
import com.luti.travel.dto.HotelBookingDto;
import com.luti.travel.service.HotelBookingService;
import com.luti.mypage.service.TravelRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentListService {

    private final PaymentListRepository paymentListRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final HotelBookingService hotelBookingService;
    private final TravelRecordService travelRecordService;

    // 결제 정보 저장
    public PaymentListResponseDTO savePayment(PaymentListRequestDTO dto) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentMethodId(dto.getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 결제 방식 ID입니다."));

        PaymentList payment = PaymentList.builder()
                .userId(dto.getUserId())
                .totalPrice(dto.getTotalPrice())
                .paymentState(0) // 0: 결제 완료
                .paymentDate(LocalDateTime.now())
                .impUid(dto.getImpUid())
                .merchantUid(dto.getMerchantUid())
                .build();

        payment.setPaymentMethod(paymentMethod);

        PaymentList saved = paymentListRepository.save(payment);
        return PaymentListResponseDTO.from(saved);
    }

    // 결제 취소 (환불 처리)
    public PaymentListResponseDTO cancelPayment(Long paymentId) {
        PaymentList payment = paymentListRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역을 찾을 수 없습니다."));

        payment.setPaymentState(1); // 1: 환불
        payment.setCancelDate(LocalDateTime.now());

        return PaymentListResponseDTO.from(payment);
    }

    // 사용자 ID로 결제 내역 조회
    public List<PaymentListResponseDTO> findByUserId(Long userId) {
        return paymentListRepository.findByUserId(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 금액 높은 순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceDesc(Long userId) {
        return paymentListRepository.findByUserIdOrderByTotalPriceDesc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 금액 낮은 순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceAsc(Long userId) {
        return paymentListRepository.findByUserIdOrderByTotalPriceAsc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제 상태 필터링 (0=결제, 1=환불)
    public List<PaymentListResponseDTO> findByUserIdAndPaymentState(Long userId, Integer paymentState) {
        return paymentListRepository.findByUserIdAndPaymentState(userId, paymentState).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 기간 필터링 (최근 1달/3달 등)
    public List<PaymentListResponseDTO> findByUserIdAndPaymentDateBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return paymentListRepository.findByUserIdAndPaymentDateBetween(userId, start, end).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제일 기준 최신순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByPaymentDateDesc(Long userId) {
        return paymentListRepository.findByUserIdOrderByPaymentDateDesc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제 상태 기준 전체 조회 (관리자용)
    public List<PaymentListResponseDTO> findByPaymentState(Integer paymentState) {
        return paymentListRepository.findByPaymentState(paymentState).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제 상태 + 날짜 범위로 조회 (관리자용)
    public List<PaymentListResponseDTO> findByPaymentStateAndDateRange(Integer state, LocalDateTime start, LocalDateTime end) {
        return paymentListRepository.findByPaymentStateAndPaymentDateBetween(state, start, end).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    public PaymentListResponseDTO savePaymentWithReservation(PaymentWithReservationDTO dto) {
        try {
            // ✅ 디버그 로그 추가
            log.info("🔍 savePaymentWithReservation 호출됨");
            log.info("🔍 fullTravelPlan 데이터: {}", dto.getFullTravelPlan() != null ? "존재" : "null");
            log.info("🔍 searchInfo 데이터: {}", dto.getSearchInfo() != null ? "존재" : "null");

            if (dto.getFullTravelPlan() != null) {
                log.info("🔍 fullTravelPlan 키: {}", dto.getFullTravelPlan().keySet());
            }

            // 1. 결제 저장
            PaymentListRequestDTO paymentDto = dto.getPayment();

            PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentMethodId(paymentDto.getPaymentMethodId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 결제 방식 ID입니다."));

            PaymentList payment = PaymentList.builder()
                    .userId(paymentDto.getUserId())
                    .totalPrice(paymentDto.getTotalPrice())
                    .paymentState(0)
                    .paymentDate(LocalDateTime.now())
                    .impUid(paymentDto.getImpUid())
                    .merchantUid(paymentDto.getMerchantUid())
                    .build();

            payment.setPaymentMethod(paymentMethod);
            PaymentList savedPayment = paymentListRepository.save(payment);

            // 2. 숙소 예약 정보 저장
            HotelBookingDto.CreateBookingRequest bookingRequest = dto.getReservation();
            hotelBookingService.createPendingBooking(bookingRequest);

            // ✅ 3. 여행 기록 자동 생성 (즐겨찾기와 동일한 방식)
            try {
                log.info("여행 기록 자동 생성 시작 - paymentId: {}, userId: {}",
                        savedPayment.getPaymentId(), savedPayment.getUserId());

                // ✅ 즐겨찾기와 동일한 데이터 구조로 저장
                Map<String, Object> travelRecordData = createTravelRecordData(
                        dto.getFullTravelPlan(),
                        dto.getSearchInfo(),
                        bookingRequest,
                        savedPayment,
                        paymentMethod
                );

                // ✅ 디버그: 최종 저장될 데이터 확인
                log.info("🔍 최종 여행 기록 데이터: {}", travelRecordData.keySet());
                if (travelRecordData.containsKey("selectedPackage")) {
                    Map<String, Object> pkg = (Map<String, Object>) travelRecordData.get("selectedPackage");
                    log.info("🔍 selectedPackage 키: {}", pkg.keySet());
                    log.info("🔍 itinerary 존재 여부: {}", pkg.containsKey("itinerary") ? "YES" : "NO");
                }

                // TravelRecordService의 일반 저장 메서드 사용
                travelRecordService.saveTravelRecord(
                        bookingRequest.getHotelName() + " 여행",
                        travelRecordData
                );

                log.info("여행 기록 자동 생성 완료 - paymentId: {}", savedPayment.getPaymentId());

            } catch (Exception e) {
                log.error("여행 기록 자동 생성 실패 - paymentId: {}, error: {}",
                        savedPayment.getPaymentId(), e.getMessage(), e);
                // 여행 기록 생성 실패해도 결제는 성공으로 처리
            }

            return PaymentListResponseDTO.from(savedPayment);

        } catch (Exception e) {
            log.error("결제 및 예약 저장 실패", e);
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다.", e);
        }
    }

   /** ✅ 즐겨찾기와 완전히 동일한 구조로 여행 기록 데이터 생성 (RouteService와 동일)
     */
    private Map<String, Object> createTravelRecordData(
            Map<String, Object> fullTravelPlan,
            Map<String, Object> searchInfo,
            HotelBookingDto.CreateBookingRequest bookingRequest,
            PaymentList savedPayment,
            PaymentMethod paymentMethod) {

        // ✅ RouteService.saveRoute()와 동일한 방식으로 전체 데이터 그대로 저장
        Map<String, Object> travelRecordData = new HashMap<>();

        if (fullTravelPlan != null && !fullTravelPlan.isEmpty()) {
            log.info("✅ 전체 여행 계획 데이터를 그대로 여행 기록에 저장 (즐겨찾기와 동일)");

            // 1. searchInfo 그대로 저장
            if (searchInfo != null && !searchInfo.isEmpty()) {
                travelRecordData.put("searchInfo", searchInfo);
            }

            // 2. selectedPackage를 fullTravelPlan 그대로 저장 (즐겨찾기와 동일)
            Map<String, Object> selectedPackage = new HashMap<>(fullTravelPlan);

            // 결제 완료 정보만 추가
            selectedPackage.put("paymentCompleted", true);
            selectedPackage.put("completedAt", LocalDateTime.now().toString());

            travelRecordData.put("selectedPackage", selectedPackage);

            // 3. 저장 시점 정보
            travelRecordData.put("savedAt", LocalDateTime.now().toString());
            travelRecordData.put("createdFromPayment", true);

        } else {
            log.warn("❌ fullTravelPlan이 없음 - 기본 호텔 정보만 저장");

            // 기본 정보만 저장하는 경우 (fallback)
            travelRecordData.put("searchInfo", Map.of(
                    "cityCode", bookingRequest.getHotelLocation(),
                    "checkInDate", bookingRequest.getCheckInDate(),
                    "checkOutDate", bookingRequest.getCheckOutDate(),
                    "adults", bookingRequest.getAdults(),
                    "nights", bookingRequest.getNights()
            ));

            travelRecordData.put("selectedPackage", Map.of(
                    "title", bookingRequest.getHotelName() + " 여행",
                    "theme", "호텔",
                    "totalPrice", savedPayment.getTotalPrice(),
                    "currency", "KRW",
                    "hotel", Map.of(
                            "name", bookingRequest.getHotelName(),
                            "location", bookingRequest.getHotelLocation(),
                            "category", "호텔"
                    ),
                    "paymentCompleted", true,
                    "completedAt", LocalDateTime.now().toString()
            ));
        }

        return travelRecordData;
    }

    /**
     * ✅ 여행 일정 정제 (selectedRoute.itinerary 구조에 맞게 수정)
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> cleanItinerary(Map<String, Object> fullTravelPlan) {
        List<Map<String, Object>> cleanedItinerary = new ArrayList<>();

        // selectedRoute.itinerary 구조로 접근
        Object itineraryObj = fullTravelPlan.get("itinerary");

        if (itineraryObj instanceof List) {
            List<Map<String, Object>> originalItinerary = (List<Map<String, Object>>) itineraryObj;
            log.debug("원본 여행 일정 {}개 발견", originalItinerary.size());

            for (Map<String, Object> day : originalItinerary) {
                Map<String, Object> cleanedDay = new HashMap<>();
                cleanedDay.put("day", day.get("day"));
                cleanedDay.put("title", day.get("title"));
                cleanedDay.put("date", day.get("date"));

                // 활동 정보 정제
                Object activitiesObj = day.get("activities");
                if (activitiesObj instanceof List) {
                    List<Map<String, Object>> originalActivities = (List<Map<String, Object>>) activitiesObj;
                    List<Map<String, Object>> cleanedActivities = new ArrayList<>();

                    for (Map<String, Object> activity : originalActivities) {
                        Map<String, Object> cleanedActivity = new HashMap<>();
                        cleanedActivity.put("time", activity.get("time"));
                        cleanedActivity.put("activity", activity.get("activity"));
                        cleanedActivity.put("location", activity.get("location"));

                        // 설명에서 불필요한 텍스트 제거
                        String description = (String) activity.get("description");
                        if (description != null) {
                            String cleanedDescription = removeUnnecessaryText(description);
                            if (!cleanedDescription.isEmpty()) {
                                cleanedActivity.put("description", cleanedDescription);
                            }
                        }

                        // included 정보는 유지
                        if (activity.get("included") != null) {
                            cleanedActivity.put("included", activity.get("included"));
                        }

                        cleanedActivities.add(cleanedActivity);
                    }
                    cleanedDay.put("activities", cleanedActivities);
                    log.debug("Day {} 활동 {}개 정제됨", day.get("day"), cleanedActivities.size());
                }
                cleanedItinerary.add(cleanedDay);
            }
        } else {
            log.warn("itinerary가 List 타입이 아님: {}", itineraryObj != null ? itineraryObj.getClass() : "null");
        }

        return cleanedItinerary;
    }

    /**
     * ✅ 하이라이트 정제
     */
    @SuppressWarnings("unchecked")
    private List<String> cleanHighlights(Map<String, Object> fullTravelPlan) {
        List<String> cleanedHighlights = new ArrayList<>();

        Object highlightsObj = fullTravelPlan.get("highlights");
        if (highlightsObj instanceof List) {
            List<String> originalHighlights = (List<String>) highlightsObj;
            for (String highlight : originalHighlights) {
                String cleaned = removeUnnecessaryText(highlight);
                if (!cleaned.isEmpty()) {
                    cleanedHighlights.add(cleaned);
                }
            }
        }

        return cleanedHighlights;
    }

    /**
     * ✅ 포함사항 정제
     */
    @SuppressWarnings("unchecked")
    private List<String> cleanIncluded(Map<String, Object> fullTravelPlan) {
        List<String> cleanedIncluded = new ArrayList<>();

        Object includedObj = fullTravelPlan.get("included");
        if (includedObj instanceof List) {
            List<String> originalIncluded = (List<String>) includedObj;
            for (String item : originalIncluded) {
                String cleaned = removeUnnecessaryText(item);
                if (!cleaned.isEmpty()) {
                    cleanedIncluded.add(cleaned);
                }
            }
        }

        return cleanedIncluded;
    }

    /**
     * ✅ 불필요한 UI 텍스트 제거
     */
    private String removeUnnecessaryText(String text) {
        if (text == null) return "";

        return text
                // 불필요한 UI 텍스트 제거
                .replaceAll("✅|체크표시|포함 글씨|즐겨보세요|먹어보세요|체험해보세요", "")
                .replaceAll("\\s*\\(추천\\)|\\s*\\(인기\\)|\\s*\\(필수\\)", "")
                .replaceAll("^\\s*[✓✔]\\s*", "") // 체크 마크 제거
                .replaceAll("\\s*[!]+\\s*", " ") // 느낌표 제거
                .replaceAll("\\s+", " ") // 연속 공백 제거
                .trim();
    }

    /**
     * ✅ 호텔 정보 정제 (selectedRoute.hotel 구조)
     */
    private Map<String, Object> cleanHotelInfo(Map<String, Object> fullTravelPlan,
                                               HotelBookingDto.CreateBookingRequest bookingRequest,
                                               PaymentList savedPayment) {
        Map<String, Object> hotelInfo = new HashMap<>();

        // selectedRoute.hotel 구조에서 호텔 정보 추출
        Object hotelObj = fullTravelPlan.get("hotel");
        if (hotelObj instanceof Map) {
            Map<String, Object> originalHotel = (Map<String, Object>) hotelObj;
            hotelInfo.put("name", getValueOrDefault(originalHotel, "name", bookingRequest.getHotelName()));
            hotelInfo.put("location", getValueOrDefault(originalHotel, "location", bookingRequest.getHotelLocation()));
            hotelInfo.put("category", getValueOrDefault(originalHotel, "category", "호텔"));
            hotelInfo.put("roomType", getValueOrDefault(originalHotel, "roomType", bookingRequest.getRoomType()));
            hotelInfo.put("pricePerNight", savedPayment.getTotalPrice() / bookingRequest.getNights() + "원");

            // 편의시설 정보도 포함 (있는 경우)
            if (originalHotel.get("amenities") != null) {
                hotelInfo.put("amenities", originalHotel.get("amenities"));
            }
        } else {
            // 기본 호텔 정보
            hotelInfo.put("name", bookingRequest.getHotelName());
            hotelInfo.put("location", bookingRequest.getHotelLocation());
            hotelInfo.put("category", "호텔");
            hotelInfo.put("roomType", bookingRequest.getRoomType() != null ? bookingRequest.getRoomType() : "스탠다드");
            hotelInfo.put("pricePerNight", savedPayment.getTotalPrice() / bookingRequest.getNights() + "원");
        }

        return hotelInfo;
    }

    /**
     * ✅ 안전한 값 추출 유틸리티
     */
    private Object getValueOrDefault(Map<String, Object> map, String key, Object defaultValue) {
        Object value = map.get(key);
        return value != null ? value : defaultValue;
    }
}