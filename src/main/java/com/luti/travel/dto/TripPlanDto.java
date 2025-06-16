package com.luti.travel.dto;

import com.google.gson.JsonArray;

public record TripPlanDto(
        String cityCode,
        String checkInDate,
        String checkOutDate,
        int    adults,
        String offerId,          // (선택) 이미 고른 객실 오퍼 ID
        JsonArray guests,
        JsonArray payments) {}
