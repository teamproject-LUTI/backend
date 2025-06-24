package com.luti.payment.service;

import com.luti.payment.dto.PaymentListRequestDTO;
import com.luti.payment.dto.PaymentListResponseDTO;
import com.luti.payment.dto.PaymentWithReservationDTO;
import com.luti.payment.entity.PaymentList;
import com.luti.payment.entity.PaymentMethod;
import com.luti.payment.repository.PaymentListRepository;
import com.luti.payment.repository.PaymentMethodRepository;
import com.luti.travel.dto.AccomodationDetailRequestDTO;
import com.luti.travel.entity.AccomodationInformation;
import com.luti.travel.service.AccomodationDetailService;
import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.travel.repository.AccomoInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentListService {

    private final PaymentListRepository paymentListRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final AccomodationDetailService accomodationDetailService;
    private final UserRepository userRepository;
    private final AccomoInfoRepository accomoInfoRepository;

    // 예약 정보 + 결제 정보를 함께 저장
    @Transactional
    public PaymentListResponseDTO savePaymentWithReservation(PaymentWithReservationDTO dto) {
        // 결제 정보 저장
        PaymentListRequestDTO paymentDto = dto.getPayment();

        PaymentList payment = PaymentList.builder()
                .userId(paymentDto.getUserId())
                .totalPrice(paymentDto.getTotalPrice())
                .paymentState(0)
                .paymentDate(LocalDateTime.now())
                .impUid(paymentDto.getImpUid())
                .merchantUid(paymentDto.getMerchantUid())
                .build();

        PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentMethodId(paymentDto.getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 결제 방식 ID입니다."));
        payment.setPaymentMethod(paymentMethod);

        PaymentList savedPayment = paymentListRepository.save(payment);

        // 예약 정보 저장
        AccomodationDetailRequestDTO reserveDto = dto.getReservation();

        User user = userRepository.findById(reserveDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
        AccomodationInformation accom = accomoInfoRepository.findById(reserveDto.getAccomodationInformationId())
                .orElseThrow(() -> new IllegalArgumentException("숙소 정보가 없습니다."));

        accomodationDetailService.saveReservation(reserveDto, savedPayment, user, accom);

        return PaymentListResponseDTO.from(savedPayment);
    }

    // 현재는 예약 통합 저장 방식 사용 중
    public PaymentListResponseDTO savePayment(PaymentListRequestDTO dto) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentMethodId(dto.getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 결제 방식 ID입니다."));

        PaymentList payment = PaymentList.builder()
                .userId(dto.getUserId())
                .totalPrice(dto.getTotalPrice())
                .paymentState(0) // 0: 결제 완료 상태
                .paymentDate(LocalDateTime.now())
                .impUid(dto.getImpUid())
                .merchantUid(dto.getMerchantUid())
                .build();

        payment.setPaymentMethod(paymentMethod);

        PaymentList saved = paymentListRepository.save(payment);
        return PaymentListResponseDTO.from(saved);
    }

    // 결제 취소 (환불 처리)
    public PaymentListResponseDTO cancelPayment(Long paymentId) {
        PaymentList payment = paymentListRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역을 찾을 수 없습니다."));

        payment.setPaymentState(1); // 1: 환불 처리
        payment.setCancelDate(LocalDateTime.now());

        return PaymentListResponseDTO.from(payment);
    }

    // 사용자 ID로 결제 내역 조회
    public List<PaymentListResponseDTO> findByUserId(Long userId) {
        return paymentListRepository.findByUserId(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 금액 높은 순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceDesc(Long userId) {
        return paymentListRepository.findByUserIdOrderByTotalPriceDesc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 금액 낮은 순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByTotalPriceAsc(Long userId) {
        return paymentListRepository.findByUserIdOrderByTotalPriceAsc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제 상태별 조회 (0=결제, 1=환불)
    public List<PaymentListResponseDTO> findByUserIdAndPaymentState(Long userId, Integer paymentState) {
        return paymentListRepository.findByUserIdAndPaymentState(userId, paymentState).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 기간 필터링 조회
    public List<PaymentListResponseDTO> findByUserIdAndPaymentDateBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return paymentListRepository.findByUserIdAndPaymentDateBetween(userId, start, end).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 결제일 기준 최신순 정렬
    public List<PaymentListResponseDTO> findByUserIdOrderByPaymentDateDesc(Long userId) {
        return paymentListRepository.findByUserIdOrderByPaymentDateDesc(userId).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 관리자용 - 결제 상태로 전체 조회
    public List<PaymentListResponseDTO> findByPaymentState(Integer paymentState) {
        return paymentListRepository.findByPaymentState(paymentState).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 관리자용 - 결제 상태 + 날짜 범위 조회
    public List<PaymentListResponseDTO> findByPaymentStateAndDateRange(Integer state, LocalDateTime start, LocalDateTime end) {
        return paymentListRepository.findByPaymentStateAndPaymentDateBetween(state, start, end).stream()
                .map(PaymentListResponseDTO::from)
                .collect(Collectors.toList());
    }

}
