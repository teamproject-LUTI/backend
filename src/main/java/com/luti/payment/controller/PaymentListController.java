package com.luti.payment.controller;

import com.luti.auth.entity.User;
import com.luti.payment.dto.PaymentListRequestDTO;
import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.service.PaymentListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentListController {

    private final PaymentListService paymentListService;

    /**
     * 1. 결제 정보 저장
     * HttpOnly 쿠키 기반 인증을 사용하므로 @AuthenticationPrincipal 사용
     */
    @PostMapping("/save")
    public ResponseEntity<PaymentListResponseDTO> savePayment(@RequestBody PaymentListRequestDTO dto) {
        PaymentListResponseDTO saved = paymentListService.savePayment(dto);
        return ResponseEntity.ok(saved);
    }

    // 2. 사용자 ID로 결제 내역 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentListResponseDTO>> getByUserId(@PathVariable Long userId) {
        List<PaymentListResponseDTO> payments = paymentListService.findByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    // 3. 환불 처리 (DB상 상태만 변경)
    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<String> cancelPayment(@PathVariable Long paymentId) {
        paymentListService.cancelPayment(paymentId);
        return ResponseEntity.ok("환불 처리 완료");
    }
}
