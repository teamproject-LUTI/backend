package com.luti.mypage.controller;

import com.luti.mypage.dto.TravelRecordDto;
import com.luti.mypage.service.TravelRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-history")
@RequiredArgsConstructor
@Slf4j
public class TravelRecordController {

    private final TravelRecordService travelRecordService;

    /**
     * 여행 기록 저장
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveTravelRecord(@RequestBody TravelRecordDto.SaveRequest request) {
        try {
            travelRecordService.saveTravelRecord(
                    request.getTravelTitle(),
                    request.getTravelContent(),
                    request.getPaymentId(),
                    request.getPaymentCd()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "여행 기록이 저장되었습니다."
            ));
        } catch (Exception e) {
            log.error("여행 기록 저장 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 사용자의 모든 여행 기록 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserTravelRecords() {
        try {
            List<TravelRecordDto.Response> records = travelRecordService.getUserTravelRecords();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "histories", records,
                    "count", records.size()
            ));
        } catch (Exception e) {
            log.error("여행 기록 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 특정 여행 기록 상세 조회
     */
    @GetMapping("/{travelRecordId}")
    public ResponseEntity<Map<String, Object>> getTravelRecordDetail(@PathVariable Long travelRecordId) {
        try {
            TravelRecordDto.DetailResponse recordDetail = travelRecordService.getTravelRecordDetail(travelRecordId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "record", recordDetail
            ));
        } catch (Exception e) {
            log.error("여행 기록 상세 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 여행 기록 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTravelStats() {
        try {
            TravelRecordDto.StatsResponse stats = travelRecordService.getTravelStats();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "stats", stats
            ));
        } catch (Exception e) {
            log.error("여행 통계 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}