package com.luti.management.controller;

import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import com.luti.management.dto.UserManagementRequestDto;
import com.luti.management.dto.UserManagementResponseDto;
import com.luti.management.service.AdminPermissionService;
import com.luti.management.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
     * 전체 사용자 목록 조회 (관리자 전용) - MultiResponseDto 직접 반환
     * GET /api/admin/users
     * @return MultiResponseDto<UserManagementResponseDto>
     */
    @GetMapping
    public ResponseEntity<MultiResponseDto<UserManagementResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.info("👥 관리자 사용자 목록 조회 요청 - 관리자 ID: {}, 페이지: {}, 크기: {}",
                    adminPermissionService.getCurrentUserId(), page, size);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            MultiResponseDto<UserManagementResponseDto> response =
                    userManagementService.getAllUsers(pageable);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("❌ 사용자 목록 조회 중 오류 발생", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "사용자 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 권한 변경 (관리자 전용) - SingleResponseDto 유지
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
     * 권한별 사용자 목록 조회 (관리자 전용) - MultiResponseDto 직접 반환
     * GET /api/admin/users/by-role?role={admin|user}
     * @param role 조회할 권한 (admin 또는 user)
     * @param page 페이지 번호 (기본값: 0) - 0-based
     * @param size 페이지 크기 (기본값: 10)
     * @return MultiResponseDto<UserManagementResponseDto>
     */
    @GetMapping("/by-role")
    public ResponseEntity<MultiResponseDto<UserManagementResponseDto>> getUsersByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
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
                throw new IllegalArgumentException("올바르지 않은 권한 값입니다. (admin 또는 user)");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            MultiResponseDto<UserManagementResponseDto> response =
                    userManagementService.getUsersByRole(isAdmin, pageable);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("❌ 권한별 사용자 목록 조회 중 오류 발생 - 권한: {}", role, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "사용자 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 검색 (관리자 전용) - MultiResponseDto 직접 반환
     * GET /api/admin/users/search?keyword={keyword}
     * @param keyword 검색 키워드
     * @param role 권한 필터 (선택사항)
     * @param page 페이지 번호 (기본값: 0) - 0-based
     * @param size 페이지 크기 (기본값: 10)
     * @return MultiResponseDto<UserManagementResponseDto>
     */
    @GetMapping("/search")
    public ResponseEntity<MultiResponseDto<UserManagementResponseDto>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            validateAdminPermission();

            if (keyword == null || keyword.trim().isEmpty()) {
                throw new IllegalArgumentException("검색 키워드가 필요합니다.");
            }

            log.info("🔍 사용자 검색 요청 - 키워드: {}, 권한: {}, 관리자 ID: {}",
                    keyword, role, adminPermissionService.getCurrentUserId());

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            MultiResponseDto<UserManagementResponseDto> response;

            // 권한 필터가 있는 경우
            if (role != null && !role.equals("all")) {
                boolean isAdmin;
                if ("admin".equalsIgnoreCase(role)) {
                    isAdmin = true;
                } else if ("user".equalsIgnoreCase(role)) {
                    isAdmin = false;
                } else {
                    throw new IllegalArgumentException("올바르지 않은 권한 값입니다. (admin 또는 user)");
                }
                response = userManagementService.searchUsersByRole(keyword.trim(), isAdmin, pageable);
            } else {
                response = userManagementService.searchUsers(keyword.trim(), pageable);
            }

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("❌ 사용자 검색 중 오류 발생 - 키워드: {}", keyword, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "사용자 검색 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 통계 정보 조회 (관리자 전용) - SingleResponseDto 유지
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