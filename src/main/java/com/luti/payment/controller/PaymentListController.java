package com.luti.payment.controller;

import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.service.PaymentListService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentListController {

    private final PaymentListService paymentListService;

    /**
     * 사용자 ID로 결제내역 전체 조회
     */
    @GetMapping("/user/{userId}")
    public List<PaymentListResponseDTO> getByUserId(@PathVariable Integer userId) {
        return paymentListService.findByUserId(userId);
    }

    /**
     * 결제 방식 코드로 결제내역 조회
     */
    @GetMapping("/method/{paymentCd}")
    public List<PaymentListResponseDTO> getByPaymentCd(@PathVariable Integer paymentCd) {
        return paymentListService.findByPaymentCd(paymentCd);
    }

    /**
     * 결제일 범위 조회
     */
    @GetMapping("/date")
    public List<PaymentListResponseDTO> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return paymentListService.findByPaymentDateBetween(start, end);
    }

    /**
     * 결제방식 기준 최신 결제 1건
     */
    @GetMapping("/latest/{paymentCd}")
    public PaymentListResponseDTO getLatestByPaymentCd(@PathVariable Integer paymentCd) {
        return paymentListService.findLatestByPaymentCd(paymentCd);
    }

    /**
     * 총 결제금액 범위 조회
     */
    @GetMapping("/price")
    public List<PaymentListResponseDTO> getByPriceRange(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        return paymentListService.findByTotalPriceRange(min, max);
    }

    /**
     * 결제 취소된 결제내역 조회
     */
    @GetMapping("/cancelled")
    public List<PaymentListResponseDTO> getAllCancelled() {
        return paymentListService.findAllCancelled();
    }

    /**
     * 사용자별 금액 오름차순 정렬
     */
    @GetMapping("/user/{userId}/asc")
    public List<PaymentListResponseDTO> getUserPaymentsAsc(@PathVariable Integer userId) {
        return paymentListService.findByUserIdOrderByTotalPriceAsc(userId);
    }

    /**
     * 사용자별 금액 내림차순 정렬
     */
    @GetMapping("/user/{userId}/desc")
    public List<PaymentListResponseDTO> getUserPaymentsDesc(@PathVariable Integer userId) {
        return paymentListService.findByUserIdOrderByTotalPriceDesc(userId);
    }
}
