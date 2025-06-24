package com.luti.travel.repository;

import com.luti.travel.entity.AccomodationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 숙소 예약 정보 저장을 위한 리포지토리
 * 결제 시 연동용으로 사용됨
 */
@Repository
public interface AccomodationDetailRepository extends JpaRepository<AccomodationDetail, Long> {
}
