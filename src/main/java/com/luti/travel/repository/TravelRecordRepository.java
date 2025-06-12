package com.luti.travel.repository;

import com.luti.auth.entity.User;
import com.luti.payment.antity.PaymentList;
import com.luti.travel.entity.TravelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRecordRepository extends JpaRepository<TravelRecord, Long> {

    // 특정 사용자가 작성한 여행 기록 전체 조회
    List<TravelRecord> findByUserId(User userId);

    // 여행 제목에 특정 키워드가 포함된 기록 조회 (LIKE 검색)
    List<TravelRecord> findByTravelTitleContaining(String keyword);

    // 특정 결제 번호(PaymentList)로 여행 기록 조회
    List<TravelRecord> findByPaymentNo(PaymentList paymentNo);

    // 특정 사용자 + 여행 제목 키워드 조합으로 조회
    List<TravelRecord> findByUserIdAndTravelTitleContaining(User userId, String keyword);


}
