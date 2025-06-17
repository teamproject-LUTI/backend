package com.luti.travel.dto;

/** 프론트 카드(UI)용으로 축약한 호텔 정보 */
public record HotelCardDto(
        String hotelId,
        String name,
        String address,
        String price,     // “123.45”
        String currency,  // “USD”
        String offerId) {}
