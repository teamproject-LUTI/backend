package com.luti.travel.dto;

import com.google.gson.JsonArray;

public class AmadeusHotelDto {             // ← 유틸리티용 껍데기 클래스

    /* ▸ 호텔 검색 요청 */
    public record HotelSearchRequest(
            String cityCode,
            String checkInDate,
            String checkOutDate,
            int adults) {}

    /* ▸ 호텔 ID 상세 요청 */
    public record HotelByIdRequest(
            String hotelId,
            String checkInDate,
            String checkOutDate,
            int adults) {}

    /* ▸ 예약 요청 */
    public record HotelBookRequest(
            String offerId,
            JsonArray guests,
            JsonArray payments) {}

}
