package com.luti.mypage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

public class RouteDto {

    /**
     * 루트 저장 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveRequest {
        private String routeTitle;
        private Map<String, Object> routeContent;
    }

    /**
     * 루트 목록 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long routeId;
        private String routeTitle;
        private String routeContent;
    }

    /**
     * 루트 상세 응답 DTO (JSON 파싱된 내용 포함)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private Long routeId;
        private String routeTitle;
        private Map<String, Object> routeData;
    }
}