package com.luti.travel.service;

import com.luti.travel.dto.AccomodationDetailRequestDTO;
import com.luti.travel.entity.AccomodationDetail;
import com.luti.travel.entity.AccomodationInformation;
import com.luti.travel.repository.AccomodationDetailRepository;
import com.luti.auth.entity.User;
import com.luti.payment.entity.PaymentList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 숙소 예약 정보 저장을 담당하는 서비스
 * 결제 시 예약 정보를 연동하는 용도로 사용
 */
@Service
@RequiredArgsConstructor
public class AccomodationDetailService {

    private final AccomodationDetailRepository accomodationDetailRepository;

    public AccomodationDetail saveReservation(AccomodationDetailRequestDTO dto, PaymentList payment, User user, AccomodationInformation accomInfo) {
        AccomodationDetail detail = new AccomodationDetail();

        detail.setPaymentId(payment);
        detail.setUserId(user);
        detail.setAccomodationInformationId(accomInfo);

        detail.setPaymentOwnno(dto.getPaymentOwnno());
        detail.setPrice(dto.getPrice());
        detail.setAccomoStart(dto.getAccomoStart());
        detail.setAccomoEnd(dto.getAccomoEnd());
        detail.setUserCount(dto.getUserCount());
        detail.setRoomType(dto.getRoomType());

        return accomodationDetailRepository.save(detail);
    }
}
