package com.luti.travel.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.resources.Hotel;
import com.amadeus.resources.HotelBooking;
import com.amadeus.resources.HotelOfferSearch;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.luti.travel.dto.TripPlanDto;
import com.luti.travel.executor.AmadeusExecutor;
import com.luti.travel.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.*;
import java.util.NoSuchElementException;

/**
 * Amadeus 호텔 검색/예약 래퍼 서비스.
 * 모든 SDK 호출을 AmadeusExecutor 로 감싸서 ExternalApiException 하나만 던진다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AmadeusHotelService {

    private final Amadeus amadeus;

    /**
     * cityCode(예: SEL, PAR) 로 조회 가능한 호텔들의 ID 목록을 얻는다.
     * Amadeus /v1/reference-data/locations/hotels/by-city 엔드포인트 사용.
     *
     * @return 호텔 ID 배열 (null·빈 배열이면 NOT_FOUND 예외)
     */
    /** ① 도시 코드 → hotelId[] */
    public String[] hotelIdsByCity(String cityCode) {
        Hotel[] hotels = AmadeusExecutor.execute("호텔ID 조회", () ->
                amadeus.referenceData.locations.hotels.byCity.get(
                        Params.with("cityCode", cityCode)
                )
        );
        if (hotels == null || hotels.length == 0) {
            throw new ExternalApiException(
                    ExternalApiException.ApiSource.AMADEUS,
                    HttpStatus.NOT_FOUND,
                    "해당 도시의 호텔ID를 찾을 수 없습니다.",
                    null
            );
        }
        return java.util.Arrays.stream(hotels)
                .map(Hotel::getHotelId)
                .map(id -> id.replace("HOTEL-", ""))  // 필요시 prefix 제거
                .toArray(String[]::new);
    }

    /** 도시/공항 코드 + 날짜 + 투숙 인원으로 최저가 호텔 목록 조회 */
    public HotelOfferSearch[] search(String cityCode,
                                     String checkIn,
                                     String checkOut,
                                     int adults) {

        String[] hotelIds = hotelIdsByCity(cityCode);

        String joinedIds  = String.join(",", hotelIds);

        log.debug("HotelOffersSearch params = hotelIds={}&checkInDate={}&checkOutDate={}&adults={}",
                joinedIds, checkIn, checkOut, adults);

        Params params = Params.with("hotelIds", joinedIds)
                .and("checkInDate",  checkIn)
                .and("checkOutDate", checkOut)
                .and("adults",       adults)
                .and("roomQuantity", 1)
                .and("bestRateOnly", "true");

        log.debug("HotelOffersSearch params = {}", params);

        return AmadeusExecutor.execute("호텔 검색", () ->
                amadeus.shopping.hotelOffersSearch.get(params)
        );
    }

    /** 단일/다중 호텔(room offers) 상세 조회 */
    public HotelOfferSearch[] byHotel(String hotelId,
                                      String checkIn,
                                      String checkOut,
                                      int adults) {

        Params params = Params.with("hotelIds", hotelId)
                .and("checkInDate",  checkIn)
                .and("checkOutDate", checkOut)
                .and("adults",       adults)
                .and("roomQuantity", "1");

        log.debug("HotelOffersSearch byHotel params = {}", params);

        return AmadeusExecutor.execute("호텔 상세 조회", () ->
                amadeus.shopping.hotelOffersSearch.get(params)
        );
    }

    /** 객실 오퍼 ID + 투숙객·결제 정보로 예약 확정 */
    public HotelBooking[] book(String offerId,
                               JsonArray guests,
                               JsonArray payments) {

        return AmadeusExecutor.execute("호텔 예약 확정", () -> {
            JsonObject data = new JsonObject();
            data.addProperty("offerId", offerId);
            data.add("guests", guests);
            data.add("payments", payments);

            JsonObject payload = new JsonObject();
            payload.add("data", data);

            return amadeus.booking.hotelBookings.post(payload);
        });
    }

    /** TripPlanDto 기반 최저가 호텔 자동 선택 후 예약 */
    public HotelBooking[] autoBook(TripPlanDto plan) {

        String offerId = plan.offerId();

        // offerId 가 없으면 최저가 호텔 1곳의 첫 번째 오퍼 선택
        if (offerId == null || offerId.isBlank()) {
            HotelOfferSearch[] list = search(
                    plan.cityCode(),
                    plan.checkInDate(),
                    plan.checkOutDate(),
                    plan.adults());

            if (list.length == 0) {
                throw new ExternalApiException(
                        ExternalApiException.ApiSource.AMADEUS,
                        HttpStatus.NOT_FOUND,
                        "해당 조건의 호텔이 없습니다.",
                        new NoSuchElementException("해당 조건의 호텔이 없습니다.")
                );
            }

            offerId = list[0].getOffers()[0].getId();
        }

        return book(offerId, plan.guests(), plan.payments());
    }
}
