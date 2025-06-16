package com.luti.travel.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.HotelOfferSearch;
import com.amadeus.resources.HotelBooking;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.luti.travel.dto.TripPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmadeusHotelService {

    private final Amadeus amadeus;

    /** 도시/공항 코드와 숙박 기간, 투숙 인원으로 기본 설정 “해당 지역의 모든 숙소 + 최저가 요금” 목록을 Amadeus에 조회 */
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

    /**  hotelIds 파라미터로 지정한 단일(또는 다중) 호텔의 모든 객실·요금·취소정책을 상세 조회 */
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

    /** 선택한 객실 오퍼 ID와 투숙객·결제 정보를 Amadeus로 전송해 예약 확정 */

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
    public HotelBooking[] autoBook(TripPlanDto plan) throws ResponseException {

        String offerId = plan.offerId();

        // 아직 offerId 가 없으면 → 최저가 호텔 1곳의 첫 번째 offer 선택
        if (offerId == null || offerId.isBlank()) {
            HotelOfferSearch[] list = search(
                    plan.cityCode(), plan.checkInDate(), plan.checkOutDate(), plan.adults());

            offerId = list[0].getOffers()[0].getId();        // 최저가 첫 건
        }

        return book(offerId, plan.guests(), plan.payments());
    }
}
