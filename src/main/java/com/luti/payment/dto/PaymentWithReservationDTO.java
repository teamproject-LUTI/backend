package com.luti.payment.dto;

import com.luti.travel.dto.AccomodationDetailRequestDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 결제 + 숙소 예약 정보를 통합적으로 받기 위한 DTO
 * 프론트에서 두 정보를 한 번에 전송하기 위해 사용
 */
@Getter
@Setter
public class PaymentWithReservationDTO {

    private PaymentListRequestDTO payment;
    private AccomodationDetailRequestDTO reservation;
}
