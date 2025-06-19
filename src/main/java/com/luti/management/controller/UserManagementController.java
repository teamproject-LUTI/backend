package com.luti.management.controller;

import com.luti.dto.SingleResponseDto;
import com.luti.management.dto.UserManagementRequestDto;
import com.luti.management.dto.UserManagementResponseDto;
import com.luti.management.service.AdminPermissionService;
import com.luti.management.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final AdminPermissionService adminPermissionService;

    /**
     * 관리자 권한 확인 - 모든 사용자 관리 API 호출 전에 권한을 검증합니다.
     */
    private void validateAdminPermission() {
        if (!adminPermissionService.isCurrentUserAdmin()) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
    }

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     * GET /api/admin/users
     * @return 사용자 목록과 통계 정보
     */
    @GetMapping // Added missing annotation
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.info("👥 관리자 사용자 목록 조회 요청 - 관리자 ID: {}, 페이지: {}, 크기: {}",
                    adminPermissionService.getCurrentUserId(), page, size);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            SingleResponseDto<Page<UserManagementResponseDto>> response =
                    userManagementService.getAllUsers(pageable);

            Page<UserManagementResponseDto> users = response.getData();
            Map<String, Object> statistics = userManagementService.getUserStatistics().getData();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "users", users.getContent(),
                    "totalElements", users.getTotalElements(),
                    "totalPages", users.getTotalPages(),
                    "currentPage", users.getNumber(),
                    "pageSize", users.getSize(),
                    "statistics", statistics,
                    "message", "사용자 목록 조회 성공"
            ));

        } catch (Exception e) {
            log.error("❌ 사용자 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "사용자 목록 조회 중 오류가 발생했습니다."
                    ));
        }
    }

    /**
     * 사용자 권한 변경 (관리자 전용)
     * POST /api/admin/users/{userId}/role
     * @param userId 권한을 변경할 사용자 ID
     * @param requestData 권한 변경 요청 데이터
     * @return 권한 변경 결과
     */
    @PostMapping("/{userId}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> requestData) {
        try {
            validateAdminPermission();

            Boolean isAdmin = (Boolean) requestData.get("isAdmin");
            if (isAdmin == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "error", "isAdmin 값이 필요합니다."
                        ));
            }

            log.info("🔄 사용자 권한 변경 요청 - 대상 사용자 ID: {}, 새 권한: {}, 관리자 ID: {}",
                    userId, isAdmin ? "관리자" : "일반사용자", adminPermissionService.getCurrentUserId());

            UserManagementRequestDto requestDto = UserManagementRequestDto.builder()
                    .isAdmin(isAdmin)
                    .build();

            SingleResponseDto<UserManagementResponseDto> response =
                    userManagementService.updateUserRole(userId, requestDto);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", response.getData(),
                    "message", "사용자 권한이 성공적으로 변경되었습니다."
            ));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("❌ 사용자 권한 변경 중 오류 발생 - 사용자 ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "권한 변경 중 오류가 발생했습니다."
                    ));
        }
    }

    /**
     * 권한별 사용자 목록 조회 (관리자 전용)
     * GET /api/admin/users/by-role?role={admin|user}
     * @param role 조회할 권한 (admin 또는 user)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 권한별 사용자 목록
     */
    @GetMapping("/by-role")
    public ResponseEntity<Map<String, Object>> getUsersByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            validateAdminPermission();

            log.info("👥 권한별 사용자 목록 조회 요청 - 권한: {}, 관리자 ID: {}",
                    role, adminPermissionService.getCurrentUserId());

            boolean isAdmin;
            if ("admin".equalsIgnoreCase(role)) {
                isAdmin = true;
            } else if ("user".equalsIgnoreCase(role)) {
                isAdmin = false;
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "error", "올바르지 않은 권한 값입니다. (admin 또는 user)"
                        ));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            SingleResponseDto<Page<UserManagementResponseDto>> response =
                    userManagementService.getUsersByRole(isAdmin, pageable);

            Page<UserManagementResponseDto> users = response.getData();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "users", users.getContent(),
                    "totalElements", users.getTotalElements(),
                    "totalPages", users.getTotalPages(),
                    "currentPage", users.getNumber(),
                    "pageSize", users.getSize(),
                    "role", role,
                    "message", role + " 권한 사용자 목록 조회 성공"
            ));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("❌ 권한별 사용자 목록 조회 중 오류 발생 - 권한: {}", role, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "사용자 목록 조회 중 오류가 발생했습니다."
                    ));
        }
    }

    /**
     * 사용자 검색 (관리자 전용)
     * GET /api/admin/users/search?keyword={keyword}
     * @param keyword 검색 키워드
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 검색된 사용자 목록
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            validateAdminPermission();

            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "error", "검색 키워드가 필요합니다."
                        ));
            }

            log.info("🔍 사용자 검색 요청 - 키워드: {}, 관리자 ID: {}",
                    keyword, adminPermissionService.getCurrentUserId());

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            SingleResponseDto<Page<UserManagementResponseDto>> response =
                    userManagementService.searchUsers(keyword.trim(), pageable);

            Page<UserManagementResponseDto> users = response.getData();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "users", users.getContent(),
                    "totalElements", users.getTotalElements(),
                    "totalPages", users.getTotalPages(),
                    "currentPage", users.getNumber(),
                    "pageSize", users.getSize(),
                    "keyword", keyword,
                    "message", "사용자 검색 완료"
            ));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("❌ 사용자 검색 중 오류 발생 - 키워드: {}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "사용자 검색 중 오류가 발생했습니다."
                    ));
        }
    }

    /**
     * 사용자 통계 정보 조회 (관리자 전용)
     * GET /api/admin/users/statistics
     * @return 사용자 통계 정보
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        try {
            validateAdminPermission();

            log.info("📊 사용자 통계 정보 조회 요청 - 관리자 ID: {}",
                    adminPermissionService.getCurrentUserId());

            SingleResponseDto<Map<String, Object>> response =
                    userManagementService.getUserStatistics();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statistics", response.getData(),
                    "message", "사용자 통계 정보 조회 성공"
            ));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("❌ 사용자 통계 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "통계 정보 조회 중 오류가 발생했습니다."
                    ));
        }
    }
}