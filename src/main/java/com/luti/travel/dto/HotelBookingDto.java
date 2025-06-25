package com.luti.travel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HotelBookingDto {

    /**
     * 숙소 예약 생성 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBookingRequest {
        // 숙소 정보
        private String hotelName;
        private String hotelLocation;
        private String hotelCategory;
        private String roomType;

        // 예약 기간
        private String checkInDate;  // LocalDate로 변환될 예정
        private String checkOutDate;
        private Integer nights;
        private Integer adults;

        // 가격 정보
        private Long pricePerNight;
        private Long totalPrice;
        private String currency;

        // 예약자 정보
        private String guestName;
        private String guestPhone;
        private String guestEmail;
        private String specialRequests;

        // 여행 패키지 정보
        private String packageId;
        private String packageTitle;
    }

    /**
     * 결제 확정 요청 DTO
     */
    public record PaymentConfirmRequest(
            Long paymentId,
            String paymentStatus,
            String transactionId
    ) {}

    /**
     * 예약 상세 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingDetailResponse {
        private Long accomodationDetailId;
        private Long bookingId;

        // 숙소 정보
        private String hotelName;
        private String hotelLocation;
        private String hotelCategory;
        private String roomType;

        // 예약 기간
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer nights;
        private Integer adults;

        // 가격 정보
        private Long pricePerNight;
        private Long totalPrice;
        private String currency;

        // 예약자 정보
        private String guestName;
        private String guestPhone;
        private String guestEmail;
        private String specialRequests;

        // 예약 상태
        private String bookingStatus;
        private LocalDateTime createdAt;
        private LocalDateTime confirmedAt;

        // 결제 정보
        private Long paymentId;
        private String paymentStatus;
    }

    /**
     * 예약 목록 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingListResponse {
        private Long accomodationDetailId;
        private String hotelName;
        private String hotelLocation;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer nights;
        private Long totalPrice;
        private String bookingStatus;
        private LocalDateTime createdAt;
    }

    /**
     * 예약 상태 Enum
     */
    public enum BookingStatus {
        PENDING,    // 결제 대기중
        CONFIRMED,  // 예약 확정
        CANCELLED   // 예약 취소
    }
}