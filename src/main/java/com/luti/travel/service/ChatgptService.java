package com.luti.travel.service;

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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatgptService {

    @Value("${chatgpt.api.key}")   // application.properties에서 주입
    private String apiKey;
    private final WebClient webClient;
    private final WebClient.Builder builder;
    private final ObjectMapper om = new ObjectMapper();

    /**
     * @param userPrompt 예: “힐링 여행 2박 3일 방콕”
     * @return find_hotels 함수-호출 argument JSON 문자열
     */
    public String getChatResponse(String userPrompt) throws JsonProcessingException {

        WebClient client = builder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // 1) 함수 정의(JSON Schema) – GPT가 ‘function_call’: {…} 로 돌려주게끔
        String functionDef = """
                [{
                  "name": "find_hotels",
                  "description": "사용자의 여행 계획에 맞춰 호텔을 검색하기 위한 파라미터 세트",
                  "parameters": {
                    "type": "object",
                    "properties": {
                      "cityCode":     { "type": "string",  "description":"IATA **3-letter** city code (예: SEL, BKK)" },
                      "checkInDate":  { "type": "string",  "description": "YYYY-MM-DD" },
                      "checkOutDate": { "type": "string",  "description": "YYYY-MM-DD" },
                      "adults":       { "type": "integer", "description": "성인 인원수" }
                    },
                    "required": ["cityCode","checkInDate","checkOutDate","adults"]
                  }
                }]
                """;

        String body = """
                {
                  "model":"gpt-4o-mini",
                  "messages":[
                    {"role":"system","content":"너는 한국어 여행 플래너야. 반드시 JSON 하나만 출력해라."},
                    {"role":"user","content":"%s"}
                  ],
                  "functions": %s,
                  "function_call":{"name":"find_hotels"}
                }
                """.formatted(userPrompt, functionDef);

        // 2) 호출
        String raw = client.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.debug("GPT raw = {}", raw);

        // 3) “arguments” 노드만 추출 → 그대로 Amadeus 쿼리 DTO로 직렬화 가능
        JsonNode argsNode = om.readTree(raw)
                .path("choices").get(0)
                .path("message").path("function_call")
                .path("arguments");
        log.debug("GPT arguments = {}", argsNode);

        if (argsNode.isObject()) {
            ObjectNode obj = (ObjectNode) argsNode;
            String cityCode = obj.path("cityCode").asText();       // "SEOUL" 같은 5글자
            if (cityCode.length() != 3) {
                String resolved = resolveCityCode(cityCode);
                if (!resolved.isBlank()) {
                    obj.put("cityCode", resolved);
                    log.debug("cityCode 보정: {} → {}", cityCode, resolved);
                } else {
                    log.warn("cityCode 보정 실패 – 원본 유지: {}", cityCode);
                }
            }
        }

        return argsNode.toString();   // 예: {"cityCode":"BKK",...}
    }

    private String resolveCityCode(String city) {

        /* ① 빠르게 사용할 수 있는 사전 매핑 */
        Map<String, String> map = Map.ofEntries(
                Map.entry("SEOUL", "SEL"), Map.entry("서울", "SEL"),
                Map.entry("BANGKOK", "BKK"), Map.entry("방콕", "BKK"),
                Map.entry("TOKYO", "TYO"), Map.entry("도쿄", "TYO"),
                Map.entry("BUSAN", "PUS"), Map.entry("부산", "PUS")
        );

        String upper = city.toUpperCase().replaceAll("\\s", ""); // 공백 제거
        if (map.containsKey(upper)) return map.get(upper);

        /* ② (선택) Amadeus Location API 호출 예시
           *실제 구현 시 amadeus bean 주입 후*
           return amadeus.referenceData.locations.get(
                   Params.with("keyword", city).and("subType","CITY"))[0]
                   .getIataCode();
        */

        return "";  // 변환 실패
    }
}
