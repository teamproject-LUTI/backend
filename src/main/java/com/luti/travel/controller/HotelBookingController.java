package com.luti.travel.controller;

import com.luti.travel.dto.HotelBookingDto;
import com.luti.travel.service.HotelBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelBookingController {

    private final HotelBookingService hotelBookingService;

    /**
     * 숙소 예약 정보 임시 저장 (결제 전)
     */
    @PostMapping("/create-booking")
    public ResponseEntity<Map<String, Object>> createBooking(
            @RequestBody HotelBookingDto.CreateBookingRequest request) {
        try {
            log.debug("숙소 예약 생성 요청: {}", request);

            Long bookingId = hotelBookingService.createPendingBooking(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "예약 정보가 저장되었습니다.",
                    "bookingId", bookingId
            ));
        } catch (Exception e) {
            log.error("숙소 예약 생성 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 결제 완료 후 예약 확정
     */
    @PostMapping("/confirm-booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> confirmBooking(
            @PathVariable Long bookingId,
            @RequestBody HotelBookingDto.PaymentConfirmRequest request) {
        try {
            log.debug("숙소 예약 확정 요청: bookingId={}, paymentId={}", bookingId, request.paymentId());

            hotelBookingService.confirmBooking(bookingId, request.paymentId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "예약이 확정되었습니다.",
                    "bookingId", bookingId
            ));
        } catch (Exception e) {
            log.error("숙소 예약 확정 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 결제 취소 시 예약 삭제
     */
    @DeleteMapping("/cancel-booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Long bookingId) {
        try {
            log.debug("숙소 예약 취소 요청: bookingId={}", bookingId);

            hotelBookingService.cancelPendingBooking(bookingId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "예약이 취소되었습니다."
            ));
        } catch (Exception e) {
            log.error("숙소 예약 취소 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 예약 상세 조회
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getBookingDetail(@PathVariable Long bookingId) {
        try {
            HotelBookingDto.BookingDetailResponse bookingDetail =
                    hotelBookingService.getBookingDetail(bookingId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "booking", bookingDetail
            ));
        } catch (Exception e) {
            log.error("숙소 예약 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 사용자별 예약 목록 조회
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<Map<String, Object>> getMyBookings() {
        try {
            var bookings = hotelBookingService.getUserBookings();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bookings", bookings,
                    "count", bookings.size()
            ));
        } catch (Exception e) {
            log.error("사용자 예약 목록 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}