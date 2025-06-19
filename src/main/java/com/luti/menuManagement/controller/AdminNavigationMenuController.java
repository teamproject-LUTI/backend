package com.luti.menuManagement.controller;

import com.luti.menuManagement.service.AdminPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
@Slf4j
public class AdminNavigationMenuController {

    private final AdminPermissionService adminPermissionService;

    /**
     * 관리자 권한 확인 - 모든 관리자 API 호출 전에 권한을 검증합니다.
     */
    private void validateAdminPermission() {
        if (!adminPermissionService.isCurrentUserAdmin()) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
    }

    /**
     * 관리자 권한 확인 API
     * @return 권한 확인 결과
     */
    @GetMapping("/auth-check")
    public ResponseEntity<Map<String, Object>> checkAdminAuth() {
        try {
            boolean isAdmin = adminPermissionService.isCurrentUserAdmin();
            Long currentUserId = adminPermissionService.getCurrentUserId();

            return ResponseEntity.ok(Map.of(
                    "isAdmin", isAdmin,
                    "userId", currentUserId,
                    "message", isAdmin ? "관리자 권한이 확인되었습니다." : "관리자 권한이 없습니다."
            ));
        } catch (Exception e) {
            log.error("관리자 권한 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "권한 확인 중 오류가 발생했습니다."));
        }
    }

    /**
     * 메뉴 목록 조회 (관리자 전용)
     * @return 메뉴 목록
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMenuList() {
        try {
            validateAdminPermission();

            // TODO: 실제 메뉴 목록 조회 로직 구현
            log.info("관리자 메뉴 목록 조회 요청 - 사용자 ID: {}",
                    adminPermissionService.getCurrentUserId());

            return ResponseEntity.ok(Map.of(
                    "message", "메뉴 목록 조회 성공",
                    "data", "TODO: 메뉴 목록 데이터"
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("메뉴 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "메뉴 목록 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 메뉴 생성 (관리자 전용)
     * @param menuData 메뉴 생성 데이터
     * @return 생성 결과
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMenu(@RequestBody Map<String, Object> menuData) {
        try {
            validateAdminPermission();

            // TODO: 실제 메뉴 생성 로직 구현
            log.info("관리자 메뉴 생성 요청 - 사용자 ID: {}, 데이터: {}",
                    adminPermissionService.getCurrentUserId(), menuData);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "메뉴 생성 성공"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("메뉴 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "메뉴 생성 중 오류가 발생했습니다."));
        }
    }

    /**
     * 메뉴 수정 (관리자 전용)
     * @param menuId 메뉴 ID
     * @param menuData 수정 데이터
     * @return 수정 결과
     */
    @PutMapping("/{menuId}")
    public ResponseEntity<Map<String, Object>> updateMenu(
            @PathVariable Long menuId,
            @RequestBody Map<String, Object> menuData) {
        try {
            validateAdminPermission();

            // TODO: 실제 메뉴 수정 로직 구현
            log.info("관리자 메뉴 수정 요청 - 사용자 ID: {}, 메뉴 ID: {}, 데이터: {}",
                    adminPermissionService.getCurrentUserId(), menuId, menuData);

            return ResponseEntity.ok(Map.of("message", "메뉴 수정 성공"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("메뉴 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "메뉴 수정 중 오류가 발생했습니다."));
        }
    }

    /**
     * 메뉴 삭제 (관리자 전용)
     * @param menuId 메뉴 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Long menuId) {
        try {
            validateAdminPermission();

            // TODO: 실제 메뉴 삭제 로직 구현
            log.info("관리자 메뉴 삭제 요청 - 사용자 ID: {}, 메뉴 ID: {}",
                    adminPermissionService.getCurrentUserId(), menuId);

            return ResponseEntity.ok(Map.of("message", "메뉴 삭제 성공"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("메뉴 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "메뉴 삭제 중 오류가 발생했습니다."));
        }
    }
}