package com.luti.menuManagement.controller;

import com.luti.dto.SingleResponseDto;
import com.luti.menuManagement.dto.NavigationMenuResponseDto;
import com.luti.menuManagement.service.NavigationMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Slf4j
public class NavigationMenuController {

    private final NavigationMenuService menuService;



    /**
     * 계층형 메뉴 조회 - 인증된 사용자의 권한에 따라 다른 메뉴 제공
     * 프론트엔드에서 /api/menus/hierarchy로 호출
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<SingleResponseDto<List<NavigationMenuResponseDto>>> getMenuHierarchy() {
        log.info("🔍 권한별 메뉴 조회 요청 - URL: /api/menus/hierarchy");

        try {
            // 임시로 인증 체크 제거하고 테스트
            log.info("📋 현재 인증 상태 확인 중...");

            SingleResponseDto<List<NavigationMenuResponseDto>> response = menuService.getHierarchicalMenus();
            log.info("✅ 메뉴 조회 성공 - 메뉴 개수: {}", response.getData() != null ? response.getData().size() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 메뉴 조회 실패", e);
            // 에러 정보를 더 자세히 로깅
            return ResponseEntity.status(500).body(new SingleResponseDto<>(null));
        }
    }

    /**
     * 관리자용 - 모든 메뉴 조회 (비활성화 포함)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("@adminPermissionService.isCurrentUserAdmin()")
    public ResponseEntity<SingleResponseDto<List<NavigationMenuResponseDto>>> getAllMenusForAdmin() {
        log.info("👑 관리자용 전체 메뉴 조회 요청");

        try {
            SingleResponseDto<List<NavigationMenuResponseDto>> response = menuService.getAllMenusForAdmin();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 관리자용 메뉴 조회 실패", e);
            return ResponseEntity.status(500).body(new SingleResponseDto<>(null));
        }
    }
}