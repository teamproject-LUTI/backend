package com.luti.payment.controller;

import com.luti.payment.dto.PaymentListRequestDTO;
import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.service.PaymentListService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentListController {

    private final PaymentListService paymentListService;

    // 결제 정보 저장
    @PostMapping("/save")
    public ResponseEntity<PaymentListResponseDTO> savePayment(@RequestBody PaymentListRequestDTO dto) {
        PaymentListResponseDTO saved = paymentListService.savePayment(dto);
        return ResponseEntity.ok(saved);
    }

    // 결제 취소 처리
    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<PaymentListResponseDTO> cancelPayment(@PathVariable Long paymentId) {
        PaymentListResponseDTO cancelled = paymentListService.cancelPayment(paymentId);
        return ResponseEntity.ok(cancelled);
    }

    // 전체 결제 내역 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentListResponseDTO>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentListService.findByUserId(userId));
    }

    // 금액 높은 순
    @GetMapping("/user/{userId}/price-desc")
    public ResponseEntity<List<PaymentListResponseDTO>> getByUserIdOrderByTotalPriceDesc(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentListService.findByUserIdOrderByTotalPriceDesc(userId));
    }

    // 금액 낮은 순
    @GetMapping("/user/{userId}/price-asc")
    public ResponseEntity<List<PaymentListResponseDTO>> getByUserIdOrderByTotalPriceAsc(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentListService.findByUserIdOrderByTotalPriceAsc(userId));
    }

    // 결제 상태 필터링 (0=결제, 1=환불)
    @GetMapping("/user/{userId}/state/{state}")
    public ResponseEntity<List<PaymentListResponseDTO>> getByUserIdAndPaymentState(@PathVariable Long userId, @PathVariable Integer state) {
        return ResponseEntity.ok(paymentListService.findByUserIdAndPaymentState(userId, state));
    }

    // 기간 필터링 (날짜 범위)
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<PaymentListResponseDTO>> getByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(paymentListService.findByUserIdAndPaymentDateBetween(userId, start, end));
    }

    // 최신순 정렬
    @GetMapping("/user/{userId}/date-desc")
    public ResponseEntity<List<PaymentListResponseDTO>> getByUserIdOrderByDateDesc(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentListService.findByUserIdOrderByPaymentDateDesc(userId));
    }
}
