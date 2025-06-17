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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ① GPT 프롬프트 → JSON
 * ② JSON 파싱 → Amadeus 호텔 검색
 * ③ 일정 + 호텔 카드 JSON 하나로 반환
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatgptController {

    private final ChatgptService      chatSvc;
    private final AmadeusHotelService hotelSvc;
    private final ObjectMapper        om = new ObjectMapper();

    @PostMapping("/ask")
    public Map<String, Object> askAndSearch(@RequestBody String prompt) throws JsonProcessingException {

        /* ── 1) GPT 호출 ─────────────────────────────────── */
        String gptJson = chatSvc.getChatResponse(prompt);

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
                    // ── 호텔 공통 정보 ──
                    String hotelId   = offerSearch.getHotel().getHotelId();
                    String hotelName = offerSearch.getHotel().getName();
                    String address   = offerSearch.getHotel().getCityCode();

                    /* 객실 오퍼별 카드 작성 */
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
        return result;
    }
}
