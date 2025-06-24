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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    // 한 번에 요청할 최대 호텔 수 (URL 길이 제한 고려)
    private static final int MAX_HOTELS_PER_REQUEST = 20;
    // 전체 검색할 최대 호텔 수 (성능 고려)
    private static final int MAX_TOTAL_HOTELS = 30;

    /**
     * cityCode(예: SEL, PAR) 로 조회 가능한 호텔들의 ID 목록을 얻는다.
     * 최대 MAX_TOTAL_HOTELS 개까지만 반환
     */
    public String[] hotelIdsByCity(String cityCode) {
        try {
            Hotel[] hotels = AmadeusExecutor.execute("호텔ID 조회", () ->
                    amadeus.referenceData.locations.hotels.byCity.get(
                            Params.with("cityCode", cityCode)
                    )
            );

            if (hotels == null || hotels.length == 0) {
                log.warn("도시 코드 {}에 대한 호텔이 없습니다.", cityCode);
                throw new ExternalApiException(
                        ExternalApiException.ApiSource.AMADEUS,
                        HttpStatus.NOT_FOUND,
                        "해당 도시의 호텔ID를 찾을 수 없습니다.",
                        null
                );
            }

            // 최대 호텔 수 제한
            int limit = Math.min(hotels.length, MAX_TOTAL_HOTELS);
            String[] hotelIds = new String[limit];

            for (int i = 0; i < limit; i++) {
                String hotelId = hotels[i].getHotelId();
                // HOTEL- prefix 제거 (필요한 경우)
                hotelIds[i] = hotelId.startsWith("HOTEL-") ?
                        hotelId.substring(6) : hotelId;
            }

            log.debug("도시 {} 호텔 ID 조회 완료: {} 개", cityCode, hotelIds.length);
            return hotelIds;

        } catch (Exception e) {
            log.error("도시 {} 호텔 ID 조회 실패: {}", cityCode, e.getMessage());
            throw new ExternalApiException(
                    ExternalApiException.ApiSource.AMADEUS,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "호텔 ID 조회 중 오류 발생: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 도시/공항 코드 + 날짜 + 투숙 인원으로 최저가 호텔 목록 조회
     * 호텔 ID 목록을 청크로 나누어 여러 번 요청
     */
    public HotelOfferSearch[] search(String cityCode,
                                     String checkIn,
                                     String checkOut,
                                     int adults) {

        String[] hotelIds = hotelIdsByCity(cityCode);

        log.debug("호텔 검색 시작 - 도시: {}, 총 호텔 수: {}", cityCode, hotelIds.length);

        List<HotelOfferSearch> allResults = new ArrayList<>();

        // 호텔 ID를 청크로 나누어 처리
        for (int i = 0; i < hotelIds.length; i += MAX_HOTELS_PER_REQUEST) {
            int endIndex = Math.min(i + MAX_HOTELS_PER_REQUEST, hotelIds.length);
            String[] chunk = Arrays.copyOfRange(hotelIds, i, endIndex);

            String joinedIds = String.join(",", chunk);

            log.debug("호텔 청크 검색 - 청크 {}/{}, 호텔 수: {}",
                    (i / MAX_HOTELS_PER_REQUEST + 1),
                    (hotelIds.length + MAX_HOTELS_PER_REQUEST - 1) / MAX_HOTELS_PER_REQUEST,
                    chunk.length);

            try {
                Params params = Params.with("hotelIds", joinedIds)
                        .and("checkInDate", checkIn)
                        .and("checkOutDate", checkOut)
                        .and("adults", adults)
                        .and("roomQuantity", 1)
                        .and("bestRateOnly", "true");

                HotelOfferSearch[] chunkResults = AmadeusExecutor.execute("호텔 검색", () ->
                        amadeus.shopping.hotelOffersSearch.get(params)
                );

                if (chunkResults != null && chunkResults.length > 0) {
                    allResults.addAll(Arrays.asList(chunkResults));
                    log.debug("청크 검색 결과: {} 개 호텔", chunkResults.length);
                }

                // API 레이트 리미트 고려하여 잠시 대기
                Thread.sleep(100);

            } catch (Exception e) {
                log.warn("호텔 청크 검색 실패 (무시하고 계속): {}", e.getMessage());
                // 일부 청크 실패해도 다른 청크는 계속 처리
            }
        }

        if (allResults.isEmpty()) {
            log.warn("도시 {}에서 검색 조건에 맞는 호텔이 없습니다.", cityCode);
            throw new ExternalApiException(
                    ExternalApiException.ApiSource.AMADEUS,
                    HttpStatus.NOT_FOUND,
                    "검색 조건에 맞는 호텔이 없습니다.",
                    null
            );
        }

        // 가격순으로 정렬하여 반환
        List<HotelOfferSearch> sortedResults = allResults.stream()
                .filter(hotel -> hotel.getOffers() != null && hotel.getOffers().length > 0)
                .sorted((h1, h2) -> {
                    try {
                        double price1 = Double.parseDouble(h1.getOffers()[0].getPrice().getTotal());
                        double price2 = Double.parseDouble(h2.getOffers()[0].getPrice().getTotal());
                        return Double.compare(price1, price2);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .limit(30) // 최대 30개 호텔만 반환
                .collect(Collectors.toList());

        log.debug("호텔 검색 완료 - 최종 결과: {} 개", sortedResults.size());
        return sortedResults.toArray(new HotelOfferSearch[0]);
    }

    /**
     * 단일/다중 호텔(room offers) 상세 조회
     */
    public HotelOfferSearch[] byHotel(String hotelId,
                                      String checkIn,
                                      String checkOut,
                                      int adults) {

        Params params = Params.with("hotelIds", hotelId)
                .and("checkInDate", checkIn)
                .and("checkOutDate", checkOut)
                .and("adults", adults)
                .and("roomQuantity", "1");

        log.debug("HotelOffersSearch byHotel params = {}", params);

        return AmadeusExecutor.execute("호텔 상세 조회", () ->
                amadeus.shopping.hotelOffersSearch.get(params)
        );
    }

    /**
     * 객실 오퍼 ID + 투숙객·결제 정보로 예약 확정
     */
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

    /**
     * TripPlanDto 기반 최저가 호텔 자동 선택 후 예약
     */
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