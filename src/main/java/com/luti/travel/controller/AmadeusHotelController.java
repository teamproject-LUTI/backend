package com.luti.travel.controller;

import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.HotelBooking;
import com.amadeus.resources.HotelOfferSearch;
import com.luti.travel.dto.*;
import com.luti.travel.service.AmadeusHotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class AmadeusHotelController {

    private final AmadeusHotelService hotelSvc;

    /** /api/hotels/search */
    @PostMapping("/search")
    public HotelOfferSearch[] search(@RequestBody AmadeusHotelDto.HotelSearchRequest r)
            throws ResponseException {

        return hotelSvc.search(
                r.cityCode(), r.checkInDate(), r.checkOutDate(), r.adults());
    }

    /** /api/hotels/by-hotel */
    @PostMapping("/by-hotel")
    public HotelOfferSearch[] byHotel(@RequestBody AmadeusHotelDto.HotelByIdRequest r)
            throws ResponseException {

        return hotelSvc.byHotel(
                r.hotelId(), r.checkInDate(), r.checkOutDate(), r.adults());
    }

    /** /api/hotels/book */
    @PostMapping("/book")
    public HotelBooking[] book(@RequestBody AmadeusHotelDto.HotelBookRequest r) // ← ‼ 주의
            throws ResponseException {

        // guests·payments는 그대로 전달
        return hotelSvc.book(r.offerId(), r.guests(), r.payments());
    }
}
