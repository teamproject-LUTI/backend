package com.luti.travel.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.payment.entity.PaymentList;
import com.luti.payment.repository.PaymentListRepository;
import com.luti.travel.dto.HotelBookingDto;
import com.luti.travel.entity.AccomodationDetail;
import com.luti.travel.entity.AccomodationInformation;
import com.luti.travel.repository.AccomoDetailRepository;
import com.luti.travel.repository.AccomoInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HotelBookingService {

    private final AccomoDetailRepository accomoDetailRepository;
    private final AccomoInfoRepository accomoInfoRepository;
    private final UserRepository userRepository;
    private final PaymentListRepository paymentListRepository;

    /**
     * 현재 로그인한 사용자 조회 (RouteService와 동일한 방식)
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // JWT 토큰 기반 인증인지 확인
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            Long userId = jwtToken.getCurrentUserId();
            String email = jwtToken.getCurrentUserEmail();

            log.debug("JWT Authentication - User ID: {}, Email: {}", userId, email);

            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));
        } else {
            // OAuth2 또는 다른 방식의 인증인 경우 이메일로 조회
            String email = authentication.getName();
            log.debug("Non-JWT Authentication - Email: {}", email);

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. email: " + email));
        }
    }

    /**
     * 예약 정보 임시 저장 (결제 전)
     */
    public Long createPendingBooking(HotelBookingDto.CreateBookingRequest request) {
        User currentUser = getCurrentUser();

        // 1. 숙소 정보 저장 또는 조회
        AccomodationInformation accomoInfo = findOrCreateAccomodationInfo(
                request.getHotelName(),
                request.getHotelLocation()
        );

        // 2. 예약 상세 정보 저장 - 빌더 패턴 사용
        AccomodationDetail accomoDetail = AccomodationDetail.builder()
                .userId(currentUser)
                .accomodationInformationId(accomoInfo)
                .accomoStart(java.sql.Date.valueOf(LocalDate.parse(request.getCheckInDate())))
                .accomoEnd(java.sql.Date.valueOf(LocalDate.parse(request.getCheckOutDate())))
                .userCount(request.getAdults().longValue())
                .roomType(request.getRoomType())
                .price(request.getTotalPrice())
                .paymentOwnno(System.currentTimeMillis()) // 임시 고유번호
                // 새로 추가된 예약자 정보
                .guestName(request.getGuestName())
                .guestPhone(request.getGuestPhone())
                .guestEmail(request.getGuestEmail())
                .specialRequests(request.getSpecialRequests())
                .nights(request.getNights())
                .currency(request.getCurrency())
                .bookingStatus("PENDING") // 결제 대기 상태
                .build();

        AccomodationDetail savedDetail = accomoDetailRepository.save(accomoDetail);

        log.info("예약 정보 임시 저장 완료: bookingId={}, user={}(ID: {})",
                savedDetail.getAccomodationDetailId(), currentUser.getEmail(), currentUser.getUserId());

        return savedDetail.getAccomodationDetailId();
    }

    /**
     * 결제 완료 후 예약 확정
     */
    public void confirmBooking(Long bookingId, Long paymentId) {
        User currentUser = getCurrentUser();

        AccomodationDetail booking = accomoDetailRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 본인의 예약인지 확인
        if (!booking.getUserId().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("본인의 예약만 확정할 수 있습니다.");
        }

        PaymentList payment = paymentListRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 결제 정보 연결 및 상태 업데이트
        booking.setPaymentId(payment);
        // TODO: 엔티티 업데이트 후 상태 변경 추가
        // booking.setBookingStatus("CONFIRMED");
        booking.setBookingStatus("CONFIRMED"); // 예약 확정 상태로 변경

        accomoDetailRepository.save(booking);

        log.info("예약 확정 완료: bookingId={}, paymentId={}, user={}(ID: {})",
                bookingId, paymentId, currentUser.getEmail(), currentUser.getUserId());
    }

    /**
     * 결제 취소 시 예약 삭제
     */
    public void cancelPendingBooking(Long bookingId) {
        User currentUser = getCurrentUser();

        AccomodationDetail booking = accomoDetailRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 본인의 예약인지 확인
        if (!booking.getUserId().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("본인의 예약만 취소할 수 있습니다.");
        }

        // 결제가 연결되지 않은 임시 예약만 삭제 가능
        if (booking.getPaymentId() == null) {
            accomoDetailRepository.delete(booking);
            log.info("임시 예약 삭제 완료: bookingId={}, user={}(ID: {})",
                    bookingId, currentUser.getEmail(), currentUser.getUserId());
        } else {
            throw new RuntimeException("이미 결제가 완료된 예약은 취소할 수 없습니다.");
        }
    }

    /**
     * 예약 상세 조회
     */
    @Transactional(readOnly = true)
    public HotelBookingDto.BookingDetailResponse getBookingDetail(Long bookingId) {
        User currentUser = getCurrentUser();

        AccomodationDetail booking = accomoDetailRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 본인의 예약인지 확인
        if (!booking.getUserId().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("본인의 예약만 조회할 수 있습니다.");
        }

        AccomodationInformation hotelInfo = booking.getAccomodationInformationId();

        return HotelBookingDto.BookingDetailResponse.builder()
                .accomodationDetailId(booking.getAccomodationDetailId())
                .bookingId(booking.getAccomodationDetailId())
                .hotelName(hotelInfo.getAccomoNm())
                .hotelLocation(getLocationFromPostNo(hotelInfo.getPostNo()))
                .roomType(booking.getRoomType())
                .checkInDate(convertToLocalDate(booking.getAccomoStart()))
                .checkOutDate(convertToLocalDate(booking.getAccomoEnd()))
                .nights(booking.getNights())
                .adults(booking.getUserCount().intValue())
                .totalPrice(booking.getPrice())
                .currency(booking.getCurrency())
                .guestName(booking.getGuestName())
                .guestPhone(booking.getGuestPhone())
                .guestEmail(booking.getGuestEmail())
                .specialRequests(booking.getSpecialRequests())
                .bookingStatus(booking.getBookingStatus())
                .createdAt(booking.getCreatedAt() != null ? booking.getCreatedAt().toInstant().atZone(java.time.ZoneId.of("Asia/Seoul")).toLocalDateTime() : null)
                .confirmedAt(booking.getConfirmedAt() != null ? booking.getConfirmedAt().toInstant().atZone(java.time.ZoneId.of("Asia/Seoul")).toLocalDateTime() : null)
                .paymentId(booking.getPaymentId() != null ? booking.getPaymentId().getPaymentId() : null)
                .build();
    }

    /**
     * 사용자별 예약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<HotelBookingDto.BookingListResponse> getUserBookings() {
        User currentUser = getCurrentUser();

        List<AccomodationDetail> bookings = accomoDetailRepository.findByUserIdOrderByAccomoStartDesc(currentUser);

        return bookings.stream()
                .map(booking -> HotelBookingDto.BookingListResponse.builder()
                        .accomodationDetailId(booking.getAccomodationDetailId())
                        .hotelName(booking.getAccomodationInformationId().getAccomoNm())
                        .hotelLocation(getLocationFromPostNo(booking.getAccomodationInformationId().getPostNo()))
                        .checkInDate(convertToLocalDate(booking.getAccomoStart()))
                        .checkOutDate(convertToLocalDate(booking.getAccomoEnd()))
                        .nights(booking.getNights())
                        .totalPrice(booking.getPrice())
                        .bookingStatus(booking.getBookingStatus())
                        .createdAt(booking.getCreatedAt() != null ? booking.getCreatedAt().toInstant().atZone(java.time.ZoneId.of("Asia/Seoul")).toLocalDateTime() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 숙소 정보 조회 또는 생성
     */
    private AccomodationInformation findOrCreateAccomodationInfo(String hotelName, String hotelLocation) {
        // 동일한 이름의 숙소가 있는지 확인
        List<AccomodationInformation> existingHotels = accomoInfoRepository.findByAccomoNm(hotelName);

        if (!existingHotels.isEmpty()) {
            return existingHotels.get(0);
        }

        // 새로운 숙소 정보 생성 - 빌더 패턴 사용
        AccomodationInformation newHotel = AccomodationInformation.builder()
                .accomoNm(hotelName)
                .postNo(generatePostNoFromLocation(hotelLocation))
                .build();

        return accomoInfoRepository.save(newHotel);
    }

    /**
     * 위치 정보에서 임시 우편번호 생성
     */
    private Long generatePostNoFromLocation(String location) {
        // 실제로는 외부 API나 데이터베이스에서 우편번호를 조회해야 하지만,
        // 여기서는 임시로 해시코드를 사용
        return (long) Math.abs(location.hashCode() % 100000);
    }

    /**
     * java.util.Date를 LocalDate로 변환하는 유틸리티 메서드 (한국 시간 기준)
     */
    private LocalDate convertToLocalDate(Date  date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(java.time.ZoneId.of("Asia/Seoul")) // 명시적으로 한국 시간대 설정
                .toLocalDate();
    }

    /**
     * 우편번호에서 위치 정보 생성
     */
    private String getLocationFromPostNo(Long postNo) {
        // 실제로는 우편번호 데이터베이스에서 조회해야 하지만,
        // 여기서는 임시로 기본값 반환
        return "서울시";
    }
}