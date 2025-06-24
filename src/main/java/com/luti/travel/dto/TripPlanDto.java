package com.luti.travel.dto;

import com.google.gson.JsonArray;

public record TripPlanDto(
        String cityCode,
        String checkInDate,
        String checkOutDate,
        int adults,
        String offerId,
        JsonArray guests,
        JsonArray payments,
        String comment
        ) {
}
