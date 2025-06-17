package com.luti.travel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ChatgptService {

    private final WebClient webClient;
    @Value("${chatgpt.api.key}")   // application.properties에서 주입
    private String apiKey;
    private final WebClient.Builder builder;
    private final ObjectMapper om = new ObjectMapper();

    /**
     * @param userPrompt   예: “힐링 여행 2박 3일 방콕”
     * @return             find_hotels 함수-호출 argument JSON 문자열
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
              "cityCode":     { "type": "string",  "description": "IATA City 코드" },
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
          "model":"gpt-4o",
          "temperature":0.3,
          "messages":[
             {"role":"system","content":"You are a travel planner. No Korean responses."},
             {"role":"user","content":%s}
          ],
          "functions": %s,
          "function_call":{"name":"find_hotels"}
        }
        """.formatted(om.writeValueAsString(userPrompt), functionDef);

        // 2) 호출
        String raw = client.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 3) “arguments” 노드만 추출 → 그대로 Amadeus 쿼리 DTO로 직렬화 가능
        JsonNode args =
                om.readTree(raw)
                        .path("choices").get(0)
                        .path("message").path("function_call")
                        .path("arguments");

        return args.toString();   // 예: {"cityCode":"BKK",...}
    }
}
