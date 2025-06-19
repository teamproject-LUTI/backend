package com.luti.management.controller;

import com.luti.dto.SingleResponseDto;
import com.luti.management.dto.NavigationMenuRequestDto;
import com.luti.management.dto.NavigationMenuResponseDto;
import com.luti.management.service.NavigationMenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@CrossOrigin(origins = "http://localhost:3000")
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

        try {
            // 임시로 인증 체크 제거하고 테스트

            SingleResponseDto<List<NavigationMenuResponseDto>> response = menuService.getHierarchicalMenus();
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
    public ResponseEntity<List<NavigationMenuResponseDto>> getAllMenusForAdmin(
            HttpServletRequest request) {


        try {
            SingleResponseDto<List<NavigationMenuResponseDto>> response = menuService.getAllMenusForAdmin();


            // 직접 List 반환하여 JSON 응답 보장
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(response.getData());

        } catch (SecurityException e) {
            log.error("❌ 권한 부족: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .header("Content-Type", "application/json")
                    .body(null);
        } catch (Exception e) {
            log.error("❌ 관리자용 메뉴 조회 실패", e);
            return ResponseEntity.status(500)
                    .header("Content-Type", "application/json")
                    .body(null);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SingleResponseDto<NavigationMenuResponseDto>> createMenu(
            @Valid @RequestBody NavigationMenuRequestDto requestDto) {

        requestDto.sanitize();

        SingleResponseDto<NavigationMenuResponseDto> result = menuService.createMenu(requestDto);
        return ResponseEntity.ok(result);
    }

    // UPDATE - 기존 RequestDto 재사용
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SingleResponseDto<NavigationMenuResponseDto>> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody NavigationMenuRequestDto requestDto) {

        requestDto.sanitize();

        SingleResponseDto<NavigationMenuResponseDto> response = menuService.updateMenu(id, requestDto);
        return ResponseEntity.ok(response);
    }

    // DELETE - ID만 필요하므로 별도 DTO 불필요
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }
}