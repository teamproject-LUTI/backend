package com.luti.mypage.controller;

import com.luti.mypage.dto.RouteDto;
import com.luti.mypage.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
public class RouteController {

    private final RouteService routeService;

    /**
     * 여행 루트를 즐겨찾기에 저장
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveRoute(@RequestBody RouteDto.SaveRequest request) {
        try {
            routeService.saveRoute(request.getRouteTitle(), request.getRouteContent());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "여행 루트가 즐겨찾기에 저장되었습니다."
            ));
        } catch (Exception e) {
            log.error("루트 저장 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 사용자의 모든 즐겨찾기 루트 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserRoutes() {
        try {
            List<RouteDto.Response> routes = routeService.getUserRoutes();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "routes", routes,
                    "count", routes.size()
            ));
        } catch (Exception e) {
            log.error("루트 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 특정 루트 상세 조회
     */
    @GetMapping("/{routeId}")
    public ResponseEntity<Map<String, Object>> getRouteDetail(@PathVariable Long routeId) {
        try {
            RouteDto.DetailResponse routeDetail = routeService.getRouteDetail(routeId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "route", routeDetail
            ));
        } catch (Exception e) {
            log.error("루트 상세 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 루트 삭제
     */
    @DeleteMapping("/{routeId}")
    public ResponseEntity<Map<String, Object>> deleteRoute(@PathVariable Long routeId) {
        try {
            routeService.deleteRoute(routeId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "루트가 삭제되었습니다."
            ));
        } catch (Exception e) {
            log.error("루트 삭제 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}