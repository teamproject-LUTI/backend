package com.luti.travel.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.HotelOfferSearch;
import com.amadeus.resources.HotelBooking;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmadeusHotelService {

    private final Amadeus amadeus;

    /** 도시·공항 코드 기반 호텔 검색 */
    public HotelOfferSearch[] search(String cityCode,
                                     String checkIn, String checkOut,
                                     int adults) throws ResponseException {

        return amadeus.shopping.hotelOffersSearch.get(
                Params.with("cityCode", cityCode)
                        .and("checkInDate",  checkIn)
                        .and("checkOutDate", checkOut)
                        .and("adults", adults)
        );
    }

    /** 특정 호텔(IDs) 객실·요금 상세 */
    public HotelOfferSearch[] byHotel(String hotelId,
                                      String checkIn, String checkOut,
                                      int adults) throws ResponseException {

        return amadeus.shopping.hotelOffersSearch.get(
                Params.with("hotelIds", hotelId)          // hotelIds 파라미터!
                        .and("checkInDate",  checkIn)
                        .and("checkOutDate", checkOut)
                        .and("adults", adults)
        );
    }

    /**
     * 예약 + 결제(샌드박스)
     */

    public HotelBooking[] book(String offerId,
                               JsonArray guests,
                               JsonArray payments) throws ResponseException {

        // 1) 요청 JSON 만들기
        JsonObject data = new JsonObject();
        data.addProperty("offerId", offerId);
        data.add("guests", guests);
        data.add("payments", payments);

        JsonObject payload = new JsonObject();
        payload.add("data", data);

        // 2) 예약 호출
        return amadeus.booking.hotelBookings.post(payload);
    }
}
