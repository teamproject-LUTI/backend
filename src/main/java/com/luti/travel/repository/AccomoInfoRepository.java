package com.luti.travel.repository;

import com.luti.travel.entity.AccomoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccomoInfoRepository extends JpaRepository<AccomoInfo, Long> {

    // 기본 단일 조회
    // 숙소 번호(PK)로 단건 조회
    Optional<AccomoInfo> findByAccomoNo(Long accomoNo);

    // 숙소 이름 기준
    // 정확한 숙소 이름으로 조회
    List<AccomoInfo> findByAccomoNm(String accomoNm);

    // 숙소 이름에 특정 키워드가 포함된 숙소 목록 조회 (LIKE %keyword%)
    List<AccomoInfo> findByAccomoNmContaining(String keyword);

    // 숙소 이름이 특정 키워드로 시작하는 경우 조회 (LIKE keyword%)
    List<AccomoInfo> findByAccomoNmStartingWith(String prefix);

    // 숙소 이름이 특정 키워드로 끝나는 경우 조회 (LIKE %keyword)
    List<AccomoInfo> findByAccomoNmEndingWith(String suffix);

    // 숙소 이름이 대소문자 무시하고 일치하는 경우 조회
    List<AccomoInfo> findByAccomoNmIgnoreCase(String accomoNm);

    // 숙소 이름 여러 개 중 일치하는 목록 조회 (IN 조건)
    List<AccomoInfo> findByAccomoNmIn(List<String> names);

    // 우편번호 기준
    // 정확한 우편번호로 숙소 조회
    List<AccomoInfo> findByPostNo(Long postNo);

    // 복합 조건
    // 숙소 이름과 우편번호가 모두 일치하는 숙소 조회
    List<AccomoInfo> findByAccomoNmAndPostNo(String accomoNm, Long postNo);
}
