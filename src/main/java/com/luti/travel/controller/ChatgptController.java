package com.luti.travel.controller;

import com.amadeus.resources.HotelOfferSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luti.travel.dto.HotelCardDto;
import com.luti.travel.service.AmadeusHotelService;
import com.luti.travel.service.ChatgptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // 👈 추가
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j  // 👈 추가
public class ChatgptController {

    private final ChatgptService      chatSvc;
    private final AmadeusHotelService hotelSvc;
    private final ObjectMapper        om = new ObjectMapper();

    /**
     * 새로운 메인 엔드포인트: 완전한 여행 패키지 생성
     */
    @PostMapping("/travel-packages")
    public Map<String, Object> generateTravelPackages(@RequestBody String prompt)
            throws JsonProcessingException {

        log.debug("여행 패키지 생성 요청: {}", prompt);

        /* ── 1) ChatGPT로 여행 패키지 생성 ─────────────────── */
        String packagesJson = chatSvc.getChatResponse(prompt);

        /* ── 2) 실제 호텔 데이터와 연동 (선택적) ─────────────── */
        String enrichedPackagesJson = chatSvc.enrichPackagesWithRealHotels(packagesJson);

        /* ── 3) JSON 파싱 및 응답 구성 ───────────────────── */
        JsonNode packagesData = om.readTree(enrichedPackagesJson);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("searchInfo", packagesData.path("searchInfo"));
        result.put("packages", packagesData.path("packages"));
        result.put("generatedAt", java.time.Instant.now().toString());
        result.put("originalPrompt", prompt);

        log.debug("여행 패키지 생성 완료: {} 개 패키지",
                packagesData.path("packages").size());

        return result;
    }

    /**
     * 선택된 여행 패키지 정보 처리
     */
    @PostMapping("/select-package")
    public Map<String, Object> selectTravelPackage(@RequestBody PackageSelectionRequest request)
            throws JsonProcessingException {

        log.debug("패키지 선택: {}", request.packageId());

        /* ── 1) 선택된 패키지 검증 ───────────────────────── */
        if (request.selectedPackage() == null) {
            throw new IllegalArgumentException("선택된 패키지 정보가 없습니다.");
        }

        /* ── 2) 예약 데이터 구성 ─────────────────────────── */
        Map<String, Object> bookingData = new HashMap<>();

        // 호텔 예약 정보
        JsonNode hotelInfo = request.selectedPackage().path("hotel");
        if (hotelInfo.has("realHotelId") && hotelInfo.has("realOfferId")) {
            // 실제 Amadeus 호텔 데이터 사용
            bookingData.put("hotelId", hotelInfo.path("realHotelId").asText());
            bookingData.put("offerId", hotelInfo.path("realOfferId").asText());
            bookingData.put("useRealHotel", true);
        } else {
            // ChatGPT 생성 호텔 정보 사용 (데모용)
            bookingData.put("hotelName", hotelInfo.path("name").asText());
            bookingData.put("useRealHotel", false);
        }

        // 검색 정보
        bookingData.put("cityCode", request.searchInfo().get("cityCode"));
        bookingData.put("checkInDate", request.searchInfo().get("checkInDate"));
        bookingData.put("checkOutDate", request.searchInfo().get("checkOutDate"));
        bookingData.put("adults", request.searchInfo().get("adults"));

        // 패키지 정보
        bookingData.put("packageId", request.packageId());
        bookingData.put("packageTitle", request.selectedPackage().path("title").asText());
        bookingData.put("totalPrice", request.selectedPackage().path("totalPrice").asText());
        bookingData.put("currency", request.selectedPackage().path("currency").asText());

        /* ── 3) 응답 구성 ─────────────────────────────────── */
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "패키지가 선택되었습니다. 예약을 진행하시겠습니까?");
        result.put("selectedPackage", request.selectedPackage());
        result.put("bookingData", bookingData);
        result.put("nextStep", "payment");
        result.put("selectedAt", java.time.Instant.now().toString());

        return result;
    }

    /**
     * 기존 호텔 검색 (호환성 유지)
     */
    @PostMapping("/ask")
    public Map<String, Object> askAndSearch(@RequestBody String prompt) throws JsonProcessingException {

        log.debug("기존 호텔 검색 요청: {}", prompt);

        /* ── 1) 기존 방식으로 호텔 검색 ─────────────────── */
        String gptJson = chatSvc.getHotelSearchResponse(prompt);  // 👈 변경: 기존 호텔 검색 메서드 사용

        /* ── 2) GPT JSON 파싱 ───────────────────────────── */
        JsonNode root        = om.readTree(gptJson);
        String   cityCode    = root.path("cityCode").asText();
        String   checkInDate = root.path("checkInDate").asText();
        String   checkOutDate= root.path("checkOutDate").asText();
        int      adults      = root.path("adults").asInt(2);

        /* ── 3) Amadeus 호텔 검색 ───────────────────────── */
        HotelOfferSearch[] offers = hotelSvc.search(cityCode, checkInDate, checkOutDate, adults);

        List<HotelCardDto> hotelCards = Stream.of(offers)
                .flatMap(offerSearch -> {
                    String hotelId   = offerSearch.getHotel().getHotelId();
                    String hotelName = offerSearch.getHotel().getName();
                    String address   = offerSearch.getHotel().getCityCode();

                    return Arrays.stream(offerSearch.getOffers())
                            .map(offer -> new HotelCardDto(
                                    hotelId,
                                    hotelName,
                                    address,
                                    offer.getPrice().getTotal(),
                                    offer.getPrice().getCurrency(),
                                    offer.getId()
                            ));
                })
                .collect(Collectors.toList());

        /* ── 4) 클라이언트 반환 JSON ─────────────────────── */
        Map<String, Object> result = new HashMap<>();
        if (root.has("plan")) {
            result.put("plan",
                    om.convertValue(root.get("plan"), new TypeReference<List<Map<String,Object>>>() {}));
        }
        result.put("hotels", hotelCards);
        result.put("searchParams", Map.of(
                "cityCode", cityCode,
                "checkInDate", checkInDate,
                "checkOutDate", checkOutDate,
                "adults", adults
        ));

        if (cityCode.isBlank())
            throw new IllegalArgumentException("GPT 응답에 cityCode 가 없습니다.");

        return result;
    }

    /**
     * 패키지 선택 요청 DTO
     */
    public record PackageSelectionRequest(
            String packageId,
            Map<String, Object> searchInfo,
            JsonNode selectedPackage,
            String userNotes
    ) {}
}