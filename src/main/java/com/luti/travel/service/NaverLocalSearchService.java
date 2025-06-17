//package com.luti.travel.service;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Service
//@RequiredArgsConstructor
//public class NaverLocalSearchService {
//
//    private final WebClient webClient;
//
//    @Value("${naver.client-id}")
//    private String clientId;
//
//    @Value("${naver.client-secret}")
//    private String clientSecret;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    //검색 조건 기본 설정
//    public String localSearch(String keyword) {
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/v1/search/local.json")
//                        //검색어
//                        .queryParam("query", keyword)
//                        .queryParam("display", 5)
//                        .queryParam("start", 1)
//                        .queryParam("sort", "random")
//                        .build())
//                .header("X-Naver-Client-Id", clientId)
//                .header("X-Naver-Client-Secret", clientSecret)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block(); // 동기 방식으로 결과 즉시 반환
//    }
//}
