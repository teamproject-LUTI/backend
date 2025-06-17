package com.luti.travel.controller;

import com.amadeus.resources.HotelBooking;
import com.amadeus.resources.HotelOfferSearch;
import com.luti.travel.dto.*;
import com.luti.travel.exception.ExternalApiException;
import com.luti.travel.service.AmadeusHotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class AmadeusHotelController {

    private final AmadeusHotelService hotelSvc;

    @PostMapping("/searchByGpt")
    public HotelOfferSearch[] searchByGpt(
            @RequestBody AmadeusHotelDto.HotelSearchRequest r) throws ExternalApiException {
        return hotelSvc.search(r.cityCode(), r.checkInDate(), r.checkOutDate(), r.adults());
    }
    /**
     AmadeusHotelDto.HotelSearchRequest
     • cityCode : TYO, PAR …
     • checkInDate : YYYY-MM-DD
     • checkOutDate : YYYY-MM-DD
     • adults : 인원수
     도시/공항 코드와 숙박 기간, 투숙 인원으로
     기본 설정 “해당 지역의 모든 숙소 + 최저가 요금” 목록을 Amadeus에 조회
     */
    @PostMapping("/search")
    public HotelOfferSearch[] search(@RequestBody AmadeusHotelDto.HotelSearchRequest r)
            throws ExternalApiException {

        return hotelSvc.search(
                r.cityCode(), r.checkInDate(), r.checkOutDate(), r.adults());
    }

    /** AmadeusHotelDto.HotelByIdRequest
     • hotelId : Amadeus 고유 호텔 ID (예: TYOHOTEL123)
     • checkInDate : YYYY-MM-DD
     • checkOutDate : YYYY-MM-DD
     • adults : 인원수
     hotelIds 파라미터로 지정한 단일(또는 다중) 호텔의 모든 객실·요금·취소정책을 상세 조회
     */
    @PostMapping("/by-hotel")
    public HotelOfferSearch[] byHotel(@RequestBody AmadeusHotelDto.HotelByIdRequest r)
            throws ExternalApiException {

        return hotelSvc.byHotel(
                r.hotelId(), r.checkInDate(), r.checkOutDate(), r.adults());
    }

    /** AmadeusHotelDto.HotelBookRequest
     • offerId : 위 단계에서 골라낸 offers[x].id (예: XJJ9BZ9R7Y)
     • guests : 투숙객 정보 JSON 배열
     • payments : 결제 수단 JSON 배열 (샌드박스 카드)
     선택한 객실 오퍼 ID와 투숙객·결제 정보를 Amadeus로 전송해 예약 확정
     */
    @PostMapping("/book")
    public HotelBooking[] book(@RequestBody AmadeusHotelDto.HotelBookRequest r)
            throws ExternalApiException {
        return hotelSvc.book(r.offerId(), r.guests(), r.payments());
    }

    @PostMapping("/auto-book")
    public HotelBooking[] autoBook(@RequestBody TripPlanDto plan)
            throws ExternalApiException {
        return hotelSvc.autoBook(plan);
    }
}
