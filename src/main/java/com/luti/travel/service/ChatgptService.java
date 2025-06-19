package com.luti.travel.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.resources.Location;
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

    @Value("${chatgpt.api.key}")              // application.properties에서 주입
    private String apiKey;

    private final WebClient.Builder builder;
    private final Amadeus amadeus;
    private final ObjectMapper om = new ObjectMapper();

    /**
     * @param userPrompt 예: “힐링 여행 2박 3일 방콕”
     * @return           {"cityCode":"BKK","checkInDate":"2025-07-01",...}
     */
    public String getChatResponse(String userPrompt) throws JsonProcessingException {

        WebClient client = builder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        /*  1. 함수 정의  */
        String functionDef = """
                [{
                  "name": "find_hotels",
                  "description": "사용자의 여행 계획에 맞춰 호텔을 검색하기 위한 파라미터 세트",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "cityCode":     { "type": "string",  "description":"IATA 3-letter code (SEL, BKK …)" },
                      "checkInDate":  { "type": "string",  "description":"YYYY-MM-DD" },
                      "checkOutDate": { "type": "string",  "description":"YYYY-MM-DD" },
                      "adults":       { "type": "integer", "description":"성인 인원" }
                    },
                    "required": ["cityCode","checkInDate","checkOutDate","adults"]
                  }
                }]""";

        /*  2. GPT 요청 바디  */
        String body = """
                {
                  "model": "gpt-4o-mini",
                  "messages": [
                    {"role":"system",
                     "content":"너는 한국어 여행 플래너야. 반드시 JSON 한 개만 출력해. \
checkInDate, checkOutDate 는 오늘(한국시간) 이후여야 한다."},
                    {"role":"user","content":"%s"}
                  ],
                  "functions": %s,
                  "function_call": {"name":"find_hotels"}
                }""".formatted(userPrompt, functionDef);

        log.debug("GPT request body = {}", body);

        /* 3. 호출  */
        String raw = client.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.debug("GPT raw = {}", raw);

        /*  4. arguments 추출  */
        JsonNode argsNode = om.readTree(raw)
                .path("choices").get(0)
                .path("message").path("function_call")
                .path("arguments");

        /*  STEP-A : 문자열 JSON → ObjectNode 변환 (항상 obj 사용) */
        ObjectNode obj = argsNode.isObject()
                ? (ObjectNode) argsNode
                : (ObjectNode) om.readTree(argsNode.asText());

        log.debug("GPT arguments (parsed) = {}", obj);

        /*  5. 도시 코드 3자리 보정 */
        String original = obj.path("cityCode").asText();
        String resolved = resolveCityCode(original);
        if (!resolved.isBlank() && !resolved.equals(original)) {
            obj.put("cityCode", resolved);
            log.debug("cityCode 보정: {} → {}", original, resolved);
        }
//        if (cityCode.length() != 3) {
//            String resolved = resolveCityCode(cityCode);
//            if (!resolved.isBlank()) {
//                obj.put("cityCode", resolved);
//                log.debug("cityCode 보정: {} → {}", cityCode, resolved);
//            } else {
//                log.warn("cityCode 보정 실패 – 원본 유지: {}", cityCode);
//            }
//        }

        /*  6. 과거 날짜 보정  */
        LocalDate today        = LocalDate.now();                // KST
        LocalDate checkInDate  = LocalDate.parse(obj.path("checkInDate").asText());

        if (!checkInDate.isAfter(today)) {                       //  “오늘/과거” 모두 교정
            checkInDate  = today.plusDays(7);                    // 1주 뒤
            LocalDate checkOutDate = checkInDate.plusDays(5);    // 5박
            obj.put("checkInDate",  checkInDate.toString());
            obj.put("checkOutDate", checkOutDate.toString());
            log.debug("❗과거 날짜 보정 → checkIn={}, checkOut={}", checkInDate, checkOutDate);
        }

        /* 7. 반환 */
        log.debug("getChatResponse() return = {}", obj.toString());
        return obj.toString();
    }
    private String resolveCityCode(String keyword) {
        if (keyword != null && keyword.matches("^[A-Z]{3}$")) {
            return keyword;
        }
        /* 1) 우선 사전 매핑 */
        Map<String, String> map = Map.ofEntries(
                Map.entry("SEOUL","SEL"), Map.entry("서울","SEL"),
                Map.entry("ICN","SEL"),   Map.entry("GMP","SEL"),
                Map.entry("BUSAN","PUS"), Map.entry("부산","PUS"),
                Map.entry("JEJU","CJU"),  Map.entry("제주","CJU"),
                Map.entry("TOKYO","TYO"), Map.entry("도쿄","TYO"),
                Map.entry("NRT","TYO"),   Map.entry("HND","TYO"),
                Map.entry("BANGKOK","BKK"), Map.entry("방콕","BKK")
        );
        String key = keyword.toUpperCase().replaceAll("\\s", "");
        if (map.containsKey(key)) return map.get(key);

        /* 2) Amadeus Locations API 실시간 조회 */
        try {
            Location[] res = amadeus.referenceData.locations.get(
                    Params.with("keyword", keyword)
                            .and("subType", "AIRPORT,CITY")
            );
            for (Location loc : res) {
                if ("CITY".equals(loc.getSubType()))
                    return loc.getIataCode();             // 바로 도시 코드
                if ("AIRPORT".equals(loc.getSubType()))
                    return loc.getAddress().getCityCode(); // 공항 → 상위 도시
            }
        } catch (Exception e) {
            log.warn("Location 변환 실패: {}", e.getMessage());
        }
        return keyword;
    }
}
