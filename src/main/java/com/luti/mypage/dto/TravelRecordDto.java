package com.luti.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

public class TravelRecordDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveRequest {
        private String travelTitle;
        private Map<String, Object> travelContent;
        private Long paymentId; // 결제와 연결할 때 사용
        private Integer paymentCd; // 결제 방법 코드
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long travelRecordId;
        private String travelTitle;
        private String travelContent;
        private Long paymentId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private Long travelRecordId;
        private String travelTitle;
        private Map<String, Object> travelData;
        private Long paymentId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsResponse {
        private long totalRecords;
        private String mostVisitedCity;
    }
}