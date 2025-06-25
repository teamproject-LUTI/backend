package com.luti.travel.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 숙소 예약 정보 요청 DTO
 * 결제와 함께 넘어오는 예약 데이터를 담는 용도
 */
@Getter
@Setter
public class AccomodationDetailRequestDTO {

    private Long paymentOwnno; // 예약 자체 식별자
    private Long accomodationInformationId; // 숙소 ID
    private Long userId; // 사용자 ID

    private Long price; // 숙박 금액
    private Date accomoStart; // 체크인
    private Date accomoEnd; // 체크아웃
    private Long userCount; // 투숙 인원
    private String roomType; // 객실 타입
}
