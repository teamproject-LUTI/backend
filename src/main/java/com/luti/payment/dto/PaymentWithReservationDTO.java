package com.luti.payment.dto;

import com.luti.travel.dto.HotelBookingDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
@Getter
@Setter
public class PaymentWithReservationDTO {
    private PaymentListRequestDTO payment;
    private HotelBookingDto.CreateBookingRequest reservation;

    // ✅ 새로 추가된 필드들
    private Map<String, Object> fullTravelPlan;  // 전체 여행 계획 데이터 (selectedRoute)
    private Map<String, Object> searchInfo;      // 검색 조건 정보
}