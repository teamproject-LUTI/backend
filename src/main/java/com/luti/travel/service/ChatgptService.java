package com.luti.travel.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.resources.Location;
import com.amadeus.resources.HotelOfferSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatgptService {

    @Value("${chatgpt.api.key}")
    private String apiKey;

    private final WebClient.Builder builder;
    private final Amadeus amadeus;
    private final AmadeusHotelService hotelSvc;
    private final ObjectMapper om = new ObjectMapper();

    /**
     * 사용자 프롬프트를 받아 호텔이 포함된 완전한 여행 패키지들을 생성
     *
     * @param userPrompt 예: "파리 2박3일", "방콕 힐링 여행 1주일"
     * @return 여러 개의 여행 패키지 옵션
     */
    public String getChatResponse(String userPrompt) throws JsonProcessingException {

        WebClient client = builder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // 한국 시간 기준으로 현재 날짜 가져오기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String todayStr = today.toString();

        // 최소 체크인 날짜 (오늘 + 3일)
        String minCheckInDate = today.plusDays(3).toString();

        /*  1. 여행 패키지 생성 함수 정의  */
        String toolsJson = String.format("""
                [{
                  "type": "function",
                  "function": {
                    "name": "create_travel_packages",
                    "description": "사용자 요청에 따라 호텔이 포함된 완전한 여행 패키지들을 생성",
                    "parameters": {
                      "type": "object",
                      "properties": {
                        "searchInfo": {
                          "type": "object",
                          "properties": {
                            "cityCode": {"type": "string", "description": "IATA 3글자 도시 코드"},
                            "checkInDate": {"type": "string", "description": "YYYY-MM-DD 형식, %s 이후"},
                            "checkOutDate": {"type": "string", "description": "YYYY-MM-DD 형식"},
                            "adults": {"type": "integer", "description": "성인 여행자 수"}
                          }
                        },
                        "packages": {
                          "type": "array",
                          "description": "3~4개의 다양한 여행 패키지 옵션",
                          "items": {
                            "type": "object",
                            "properties": {
                              "packageId": {"type": "string", "description": "패키지 고유 ID"},
                              "title": {"type": "string", "description": "패키지 제목"},
                              "theme": {"type": "string", "description": "여행 테마"},
                              "priceRange": {"type": "string", "description": "가격대 (저렴/중간/고급)"},
                              "totalPrice": {"type": "string", "description": "총 여행 비용"},
                              "currency": {"type": "string", "description": "화폐 단위"},
                              "hotel": {
                                "type": "object",
                                "description": "당일치기가 아닌 경우에만 필요",
                                "properties": {
                                  "name": {"type": "string", "description": "호텔명"},
                                  "category": {"type": "string", "description": "호텔 등급 (3성/4성/5성)"},
                                  "location": {"type": "string", "description": "호텔 위치"},
                                  "pricePerNight": {"type": "string", "description": "1박 요금"},
                                  "amenities": {"type": "array", "items": {"type": "string"}},
                                  "highlights": {"type": "array", "items": {"type": "string"}}
                                }
                              },
                              "itinerary": {
                                "type": "array",
                                "items": {
                                  "type": "object",
                                  "properties": {
                                    "day": {"type": "integer"},
                                    "date": {"type": "string", "description": "YYYY-MM-DD 형식"},
                                    "title": {"type": "string"},
                                    "activities": {
                                      "type": "array",
                                      "items": {
                                        "type": "object",
                                        "properties": {
                                          "time": {"type": "string"},
                                          "activity": {"type": "string"},
                                          "location": {"type": "string"},
                                          "description": {"type": "string"},
                                          "included": {"type": "boolean", "description": "패키지 포함 여부"}
                                        }
                                      }
                                    }
                                  }
                                }
                              },
                              "included": {
                                "type": "array",
                                "description": "패키지에 포함된 항목들",
                                "items": {"type": "string"}
                              },
                              "highlights": {
                                "type": "array",
                                "description": "패키지 하이라이트",
                                "items": {"type": "string"}
                              },
                              "bestFor": {"type": "string", "description": "추천 대상"}
                            }
                          }
                        }
                      },
                      "required": ["searchInfo", "packages"]
                    }
                  }
                }]""", minCheckInDate);

        /*  2. 시스템 프롬프트  */
        String systemContent = String.format("""
            당신은 전문 여행 루트 기획자입니다. 사용자의 요청에 따라 호텔과 상세한 일정이 포함된 여행 루트 옵션들을 만들어주세요.
    
            중요한 규칙:
            - 오늘 날짜: %s (한국시간)
            - 체크인 날짜는 반드시 %s 이후여야 합니다
            - 모든 날짜는 YYYY-MM-DD 형식으로 작성
            - currency 화폐단위는 원화로 환산
            - 최소 3개 이상의 추천 루트 생성
            
            숙박 판단 기준:
            - "당일치기", "데이트리", "하루", "일일" 등이 포함된 요청 → 당일 여행
            - 당일치기의 경우: checkInDate와 checkOutDate를 같은 날짜로 설정
            - 당일치기의 경우: hotel 객체를 생성하지 마세요
            - "1박2일", "2박3일" 등 숙박이 명시된 경우 → 숙박 여행
            
            여행 기간 계산:
            - "1박2일" = checkInDate부터 1박 → checkOutDate는 checkInDate + 1일
            - "2박3일" = checkInDate부터 2박 → checkOutDate는 checkInDate + 2일
            - "1주일" = 6박7일로 계산
            
            일정 구성:
            - 당일치기: 하루 일정만 생성 (day: 1)
            - 숙박 여행: 박수 + 1일만큼 일정 생성
            - 각 일정에는 반드시 activities 배열이 있어야 함
            - activities는 비어있으면 안 됨
            - 당일치기에서는 "호텔 체크인/체크아웃" 활동 절대 포함 금지
            
            반드시 3~4개의 서로 다른 여행 루트를 만들고 create_travel_packages 함수만 호출하세요.
            """, todayStr, minCheckInDate);

        /*  3. 사용자 메시지  */
        String userMessage = String.format("""
            다음 여행 요청에 대해 3-4개의 다양한 여행 패키지를 만들어주세요:
            
            요청: %s
            
            중요 사항:
            - 체크인 날짜는 %s 이후여야 합니다
            - 일정의 각 day마다 반드시 activities 배열을 포함해야 합니다
            - activities는 비어있으면 안 됩니다
            - 당일치기인 경우 hotel 객체를 만들지 마세요
            - 날짜는 모두 YYYY-MM-DD 형식으로 작성해주세요
            """, userPrompt, minCheckInDate);

        /*  4. GPT 요청 바디 생성  */
        String requestBody = String.format("""
                {
                  "model": "gpt-4o-mini",
                  "messages": [
                    {"role":"system", "content":%s},
                    {"role":"user", "content":%s}
                  ],
                  "tools": %s,
                  "tool_choice": {"type": "function", "function": {"name": "create_travel_packages"}},
                  "temperature": 0.8,
                  "max_tokens": 8000
                }""",
                om.writeValueAsString(systemContent),
                om.writeValueAsString(userMessage),
                toolsJson);

        log.debug("GPT request body = {}", requestBody);

        /* 5. API 호출 */
        String rawResponse = client.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.debug("GPT raw response = {}", rawResponse);

        /*  6. 응답 파싱  */
        JsonNode responseRoot = om.readTree(rawResponse);
        JsonNode toolCallsNode = responseRoot
                .path("choices").get(0)
                .path("message")
                .path("tool_calls");

        if (toolCallsNode.isEmpty()) {
            throw new RuntimeException("여행 패키지 생성 실패: " + rawResponse);
        }

        JsonNode argsNode = toolCallsNode.get(0)
                .path("function")
                .path("arguments");

        ObjectNode result = argsNode.isObject()
                ? (ObjectNode) argsNode
                : (ObjectNode) om.readTree(argsNode.asText());

        // 도시 코드 보정
        JsonNode searchInfo = result.path("searchInfo");
        if (searchInfo.has("cityCode")) {
            String originalCityCode = searchInfo.path("cityCode").asText();
            String resolvedCityCode = resolveCityCode(originalCityCode);
            if (!resolvedCityCode.equals(originalCityCode)) {
                ((ObjectNode) searchInfo).put("cityCode", resolvedCityCode);
                log.debug("도시코드 보정: {} → {}", originalCityCode, resolvedCityCode);
            }
        }

        // 날짜 검증 및 보정
        validateAndFixDates(result, today);

        log.debug("생성된 여행 패키지: {}", result.toString());
        return result.toString();
    }

    /**
     * 날짜 검증 및 보정
     */
    private void validateAndFixDates(ObjectNode result, LocalDate today) {
        JsonNode searchInfo = result.path("searchInfo");
        if (searchInfo.has("checkInDate")) {
            String checkInDateStr = searchInfo.path("checkInDate").asText();
            String checkOutDateStr = searchInfo.path("checkOutDate").asText();

            try {
                LocalDate checkInDate = LocalDate.parse(checkInDateStr);
                LocalDate checkOutDate = LocalDate.parse(checkOutDateStr);

                // 체크인 날짜가 과거인 경우 보정
                if (!checkInDate.isAfter(today.plusDays(2))) {
                    LocalDate newCheckIn = today.plusDays(3);
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                    LocalDate newCheckOut = newCheckIn.plusDays(nights);

                    ((ObjectNode) searchInfo).put("checkInDate", newCheckIn.toString());
                    ((ObjectNode) searchInfo).put("checkOutDate", newCheckOut.toString());

                    log.debug("날짜 보정: {} → {}, {} → {}",
                            checkInDateStr, newCheckIn, checkOutDateStr, newCheckOut);

                    // 패키지들의 일정 날짜도 업데이트
                    updatePackagesDates(result, newCheckIn);
                }
            } catch (Exception e) {
                log.warn("날짜 파싱 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * 패키지들의 일정 날짜 업데이트
     */
    private void updatePackagesDates(ObjectNode result, LocalDate newCheckIn) {
        JsonNode packages = result.path("packages");
        if (packages.isArray()) {
            for (JsonNode packageNode : packages) {
                JsonNode itinerary = packageNode.path("itinerary");
                if (itinerary.isArray()) {
                    for (int i = 0; i < itinerary.size(); i++) {
                        ObjectNode dayNode = (ObjectNode) itinerary.get(i);
                        LocalDate dayDate = newCheckIn.plusDays(i);
                        dayNode.put("date", dayDate.toString());
                    }
                }
            }
        }
    }

    /**
     * 실제 호텔 데이터와 연동하여 패키지 보강 (선택적)
     */
    public String enrichPackagesWithRealHotels(String packagesJson) throws JsonProcessingException {
        JsonNode packages = om.readTree(packagesJson);
        ObjectNode enriched = (ObjectNode) packages;

        // 실제 Amadeus API에서 가져온 호텔 데이터로 보강
        JsonNode searchInfo = packages.path("searchInfo");
        String cityCode = searchInfo.path("cityCode").asText();
        String checkInDate = searchInfo.path("checkInDate").asText();
        String checkOutDate = searchInfo.path("checkOutDate").asText();
        int adults = searchInfo.path("adults").asInt();

        try {
            // 실제 호텔 데이터 조회
            HotelOfferSearch[] realHotels = hotelSvc.search(cityCode, checkInDate, checkOutDate, adults);

            if (realHotels.length > 0) {
                // ChatGPT가 생성한 패키지의 호텔 정보를 실제 데이터로 업데이트
                JsonNode packagesArray = enriched.path("packages");
                if (packagesArray.isArray()) {
                    for (int i = 0; i < Math.min(packagesArray.size(), realHotels.length); i++) {
                        ObjectNode packageNode = (ObjectNode) packagesArray.get(i);
                        JsonNode hotelNode = packageNode.path("hotel");

                        // 당일치기인 경우 호텔 정보가 없을 수 있음
                        if (!hotelNode.isMissingNode() && hotelNode.isObject()) {
                            ObjectNode hotelObj = (ObjectNode) hotelNode;
                            HotelOfferSearch realHotel = realHotels[i];
                            hotelObj.put("realHotelId", realHotel.getHotel().getHotelId());
                            hotelObj.put("realOfferId", realHotel.getOffers()[0].getId());
                            hotelObj.put("actualPrice", realHotel.getOffers()[0].getPrice().getTotal());
                            hotelObj.put("actualCurrency", realHotel.getOffers()[0].getPrice().getCurrency());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("실제 호텔 데이터 연동 실패, ChatGPT 데이터만 사용: {}", e.getMessage());
        }

        return enriched.toString();
    }

    /**
     * 기존 호텔 검색 메서드 (호환성 유지)
     */
    public String getHotelSearchResponse(String userPrompt) throws JsonProcessingException {
        WebClient client = builder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String todayStr = today.toString();

        String toolsJson = String.format("""
                [{
                  "type": "function",
                  "function": {
                    "name": "find_hotels",
                    "description": "사용자의 여행 계획에 맞춰 호텔을 검색하기 위한 파라미터 세트",
                    "parameters": {
                      "type": "object",
                      "properties": {
                        "cityCode":     { "type": "string",  "description":"IATA 3-letter code (PAR, SEL, TYO, BKK 등)" },
                        "checkInDate":  { "type": "string",  "description":"YYYY-MM-DD 형식, 반드시 %s 이후" },
                        "checkOutDate": { "type": "string",  "description":"YYYY-MM-DD 형식" },
                        "adults":       { "type": "integer", "description":"성인 인원" }
                      },
                      "required": ["cityCode","checkInDate","checkOutDate","adults"]
                    }
                  }
                }]""", todayStr);

        String systemContent = String.format("""
            너는 한국어 여행 플래너야. 다음 규칙을 반드시 지켜:
            
            📅 날짜 규칙:
            - 오늘: %s (한국시간)  
            - 체크인은 반드시 오늘 + 3일 이후
            - "2박3일"이면 정확히 2일 차이
            - "1주일"이면 6박7일로 계산
            
            🏙️ 도시코드:
            - 파리=PAR, 서울=SEL, 도쿄=TYO, 방콕=BKK, 제주=CJU
            - 반드시 정확한 IATA 3글자 도시 코드 사용
            
            반드시 find_hotels 함수만 호출해.
            """, todayStr);

        String requestBody = String.format("""
                {
                  "model": "gpt-4o-mini",
                  "messages": [
                    {"role":"system", "content":%s},
                    {"role":"user", "content":%s}
                  ],
                  "tools": %s,
                  "tool_choice": {"type": "function", "function": {"name": "find_hotels"}},
                  "temperature": 0.3
                }""",
                om.writeValueAsString(systemContent),
                om.writeValueAsString(userPrompt),
                toolsJson);

        String rawResponse = client.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode responseRoot = om.readTree(rawResponse);
        JsonNode toolCallsNode = responseRoot.path("choices").get(0)
                .path("message").path("tool_calls");

        if (toolCallsNode.isEmpty()) {
            throw new RuntimeException("GPT 응답에 tool_calls가 없습니다: " + rawResponse);
        }

        JsonNode argsNode = toolCallsNode.get(0).path("function").path("arguments");
        ObjectNode obj = argsNode.isObject()
                ? (ObjectNode) argsNode
                : (ObjectNode) om.readTree(argsNode.asText());

        // 도시 코드 보정
        String original = obj.path("cityCode").asText();
        String resolved = resolveCityCode(original);
        if (!resolved.equals(original)) {
            obj.put("cityCode", resolved);
            log.debug("cityCode 보정: {} → {}", original, resolved);
        }

        // 날짜 보정
        LocalDate checkInDate = LocalDate.parse(obj.path("checkInDate").asText());
        if (!checkInDate.isAfter(today.plusDays(2))) {
            int nights = extractNightsFromPrompt(userPrompt);
            checkInDate = today.plusDays(3);
            LocalDate checkOutDate = checkInDate.plusDays(nights);

            obj.put("checkInDate", checkInDate.toString());
            obj.put("checkOutDate", checkOutDate.toString());

            log.debug("날짜 보정 → checkIn={}, checkOut={} ({}박)",
                    checkInDate, checkOutDate, nights);
        }

        return obj.toString();
    }

    /**
     * 사용자 프롬프트에서 박수 추출
     */
    private int extractNightsFromPrompt(String prompt) {
        if (prompt == null) return 1;

        String p = prompt.toLowerCase();

        // "2박3일" 패턴
        if (p.contains("박")) {
            try {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)박");
                java.util.regex.Matcher matcher = pattern.matcher(p);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            } catch (Exception e) {
                log.warn("박수 추출 실패: {}", prompt);
            }
        }

        // "1주일" 패턴
        if (p.contains("주일") || p.contains("주")) {
            return 6; // 6박7일
        }

        // "5일" 패턴 (일수-1 = 박수)
        if (p.contains("일")) {
            try {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)일");
                java.util.regex.Matcher matcher = pattern.matcher(p);
                if (matcher.find()) {
                    int days = Integer.parseInt(matcher.group(1));
                    return Math.max(1, days - 1);
                }
            } catch (Exception e) {
                log.warn("일수 추출 실패: {}", prompt);
            }
        }

        return 1; // 기본값을 1박으로 변경
    }

    /**
     * 도시코드 변환
     */
    private String resolveCityCode(String keyword) {
        if ("CDG".equals(keyword)) return "PAR";  // 파리 공항코드 보정
        if (keyword != null && keyword.matches("^[A-Z]{3}$")) {
            return keyword;
        }

        /* 1) 우선 사전 매핑 */
        Map<String, String> cityMap = Map.ofEntries(
                // 한국
                Map.entry("서울", "SEL"), Map.entry("SEOUL", "SEL"),
                Map.entry("ICN", "SEL"), Map.entry("GMP", "SEL"),
                Map.entry("부산", "PUS"), Map.entry("BUSAN", "PUS"),
                Map.entry("제주", "CJU"), Map.entry("JEJU", "CJU"),

                // 일본
                Map.entry("도쿄", "TYO"), Map.entry("TOKYO", "TYO"),
                Map.entry("NRT", "TYO"), Map.entry("HND", "TYO"),
                Map.entry("오사카", "OSA"), Map.entry("OSAKA", "OSA"),

                // 동남아시아
                Map.entry("방콕", "BKK"), Map.entry("BANGKOK", "BKK"),
                Map.entry("싱가포르", "SIN"), Map.entry("SINGAPORE", "SIN"),

                // 유럽
                Map.entry("파리", "PAR"), Map.entry("PARIS", "PAR"),
                Map.entry("CDG", "PAR"), Map.entry("ORY", "PAR"),
                Map.entry("런던", "LON"), Map.entry("LONDON", "LON"),
                Map.entry("LHR", "LON"), Map.entry("LGW", "LON"),
                Map.entry("로마", "ROM"), Map.entry("ROME", "ROM"),
                Map.entry("FCO", "ROM"), Map.entry("CIA", "ROM"),

                // 북미
                Map.entry("뉴욕", "NYC"), Map.entry("NEW YORK", "NYC"),
                Map.entry("JFK", "NYC"), Map.entry("LGA", "NYC"), Map.entry("EWR", "NYC"),
                Map.entry("LA", "LAX"), Map.entry("로스앤젤레스", "LAX"),
                Map.entry("LOS ANGELES", "LAX")
        );

        String normalizedKeyword = keyword.toUpperCase().trim().replaceAll("\\s", "");
        if (cityMap.containsKey(normalizedKeyword)) {
            return cityMap.get(normalizedKeyword);
        }

        /* 2) Amadeus Locations API 실시간 조회 */
        try {
            Location[] locations = amadeus.referenceData.locations.get(
                    Params.with("keyword", keyword)
                            .and("subType", "AIRPORT,CITY")
            );

            // 도시 코드 우선 검색
            for (Location loc : locations) {
                if ("CITY".equals(loc.getSubType()) && loc.getIataCode() != null) {
                    log.debug("도시 코드 발견: {} → {}", keyword, loc.getIataCode());
                    return loc.getIataCode();
                }
            }

            // 공항의 도시 코드 검색
            for (Location loc : locations) {
                if ("AIRPORT".equals(loc.getSubType()) &&
                        loc.getAddress() != null &&
                        loc.getAddress().getCityCode() != null) {
                    log.debug("공항 도시 코드 발견: {} → {}", keyword, loc.getAddress().getCityCode());
                    return loc.getAddress().getCityCode();
                }
            }

        } catch (Exception e) {
            log.warn("Amadeus Location API 호출 실패: {}", e.getMessage());
        }

        log.warn("도시 코드 변환 실패, 원본 반환: {}", keyword);
        return keyword;
    }
}