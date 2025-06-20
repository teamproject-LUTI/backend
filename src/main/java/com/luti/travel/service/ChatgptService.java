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

        // 현재 한국 시간
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

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
                            "checkInDate": {"type": "string", "description": "YYYY-MM-DD 형식"},
                            "checkOutDate": {"type": "string", "description": "YYYY-MM-DD 형식"},
                            "adults": {"type": "integer", "description": "성인 여행자 수"}
                          }
                        },
                        "packages": {
                          "type": "array",
                          "description": "3-4개의 다양한 여행 패키지 옵션",
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
                                    "date": {"type": "string"},
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
                }]""", todayStr);

        /*  2. 시스템 프롬프트  */
        String systemContent = String.format("""
            당신은 전문 여행 루트 기획자입니다. 사용자의 요청에 따라 호텔과 상세한 일정이 포함된 여행 루트 옵션들을 만들어주세요.
    
    여행 루트 생성 가이드라인:
    
    루트 다양성:
    - 가성비 루트: 저렴한 호텔 + 무료/저렴한 관광지 중심
    - 균형형 루트: 중급 호텔 + 인기 관광지와 현지 체험
    - 프리미엄 루트: 고급 호텔 + 특별 체험과 VIP 투어
    - 테마 특화 루트: 힐링, 문화, 미식, 쇼핑 등 특정 테마 중심
    
    숙박 판단:
    - "당일치기", "데이트리", "하루" 등이 포함된 요청은 숙박 없는 당일 여행
    - 당일치기의 경우: checkInDate와 checkOutDate를 같은 날짜로 설정
    - 당일치기의 경우: hotel 객체를 아예 생성하지 마세요
    - 숙박이 포함된 경우: 각 루트마다 적절한 등급의 호텔 정보
    - 숙박이 포함된 경우: 위치, 시설, 1박 요금 정보 포함
    
    일정 구성 및 상세:
    - 당일치기는 하루 일정만 생성(day1)
    - 당일치기에서는 "호텔 체크인", "호텔 체크아웃" 활동을 절대 포함하지 마세요
    - 요청된 기간이 있을 경우 일수에 맞게 생성
    - 시간별 구체적인 활동 계획
    - 이동 경로와 교통편 고려
    - 식사, 관광, 휴식 시간 균형 배치
    - 각 활동의 소요시간과 비용 안내
    
    
    비용 계산:
    - 호텔, 식사, 교통, 입장료 등 총 비용
    - 한국 원화 기준으로 정확한 가격 산정
    - 당일치기는 교통비, 식사비, 입장료 등 만 포함
    - 숙박 여행은 호텔 비용 포함
    
    반드시 3-4개의 서로 다른 여행 루트를 만들고 create_travel_packages 함수만 호출하세요.
    """, todayStr);

        /*  3. 사용자 메시지  */
        String userMessage = String.format("""
            다음 여행 요청에 대해 3-4개의 다양한 여행 패키지를 만들어주세요:
            
            요청: %s
            
            각 패키지는 다음을 포함해야 합니다:
            - 서로 다른 가격대의 호텔
            - 상세한 일정 (시간, 장소, 활동)
            - 포함사항과 불포함사항 명시
            - 패키지별 특색과 하이라이트
            - 현실적인 총 비용 (한국 원화)
            
            패키지 유형 예시:
            1. 가성비 패키지 (저렴한 호텔 + 기본 일정)
            2. 추천 패키지 (중급 호텔 + 균형잡힌 일정)
            3. 프리미엄 패키지 (고급 호텔 + 특별 경험)
            4. 테마 특화 패키지 (특정 테마 중심)
            """, userPrompt);

        /*  4. GPT 요청 바디 생성  */
        String requestBody = String.format("""
                {
                  "model": "gpt-4o-mini",
                  "messages": [
<<<<<<< HEAD
                    {"role":"system", "content":%s},
                    {"role":"user", "content":%s}
=======
                    {"role":"system",
                     "content":"너는 한국어 여행 플래너야. 반드시 JSON 한 개만 출력해. \
                     checkInDate, checkOutDate 는 오늘(한국시간) 이후여야 한다. 일정을 JSON 으로, 설명은 comment 필드에 한국어 텍스트로 넣어라"},
                    {"role":"user","content":"%s"}
>>>>>>> 2072445ae649c0d62adaef39aae331f6c2d7a3ee
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

        log.debug("생성된 여행 패키지: {}", result.toString());
        return result.toString();
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
                        ObjectNode hotelNode = (ObjectNode) packageNode.path("hotel");

                        HotelOfferSearch realHotel = realHotels[i];
                        hotelNode.put("realHotelId", realHotel.getHotel().getHotelId());
                        hotelNode.put("realOfferId", realHotel.getOffers()[0].getId());
                        hotelNode.put("actualPrice", realHotel.getOffers()[0].getPrice().getTotal());
                        hotelNode.put("actualCurrency", realHotel.getOffers()[0].getPrice().getCurrency());
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

        LocalDate today = LocalDate.now();
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
            - 체크인은 반드시 오늘 이후
            - "2박3일"이면 정확히 2일 차이
            - "1주일"이면 6박7일로 계산
            
            🏙️ 도시코드:
            - 파리=PAR, 서울=SEL, 도쿄=TYO, 방콕=BKK
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
        if (!checkInDate.isAfter(today)) {
            int nights = extractNightsFromPrompt(userPrompt);
            checkInDate = today.plusDays(7);
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
        if (prompt == null) return 2;

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

        return 2; // 기본값
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