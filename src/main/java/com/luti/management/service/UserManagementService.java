package com.luti.management.service;

import com.luti.auth.entity.User;
import com.luti.auth.entity.UserType; // Assuming this entity exists
import com.luti.auth.enums.UserTypeEnum;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.repository.UserTypeRepository; // Assuming this repository exists
import com.luti.dto.SingleResponseDto;
import com.luti.management.dto.UserManagementRequestDto;
import com.luti.management.dto.UserManagementResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserManagementService {
    private final UserRepository userRepository;
    private final AdminPermissionService adminPermissionService;
    private final UserTypeRepository userTypeRepository;

    /**
     * 모든 사용자 목록 조회 (페이징)
     */
    public SingleResponseDto<Page<UserManagementResponseDto>> getAllUsers(Pageable pageable) {
        adminPermissionService.requireAdminPermission("사용자 목록 조회");

        try {
            Page<User> users = userRepository.findAllActiveUsers(pageable);

            Page<UserManagementResponseDto> userDtos = users.map(UserManagementResponseDto::fromEntity);

            return new SingleResponseDto<>(userDtos);

        } catch (Exception e) {
            log.error("❌ 사용자 목록 조회 실패", e);
            throw new RuntimeException("사용자 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 권한 변경 - 유일한 수정 가능 기능
     */
    @Transactional
    public SingleResponseDto<UserManagementResponseDto> updateUserRole(Long userId, UserManagementRequestDto requestDto) {
        adminPermissionService.requireAdminPermission("사용자 권한 변경");

        try {

            // 1. 현재 관리자 확인
            Long currentAdminId = adminPermissionService.getCurrentUserId();

            // 2. 대상 사용자 조회
            User targetUser = userRepository.findByUserId(userId);
            if (targetUser == null) {
                log.error("❌ 사용자를 찾을 수 없음 - 사용자 ID: {}", userId);
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId);
            }
            // 3. 유효성 검사
            if ("Y".equals(targetUser.getWithdrawYn())) {
                log.error("❌ 탈퇴한 사용자의 권한 변경 시도 - 사용자 ID: {}", userId);
                throw new IllegalArgumentException("탈퇴한 사용자의 권한은 변경할 수 없습니다.");
            }

            if (currentAdminId.equals(userId) && !requestDto.getIsAdmin()) {
                log.error("❌ 자기 자신의 관리자 권한 해제 시도 - 사용자 ID: {}", userId);
                throw new IllegalArgumentException("자기 자신의 관리자 권한을 해제할 수 없습니다.");
            }

            // 4. 현재 권한 확인
            boolean currentIsAdmin = targetUser.getUserTypeId() != null &&
                    UserTypeEnum.ADMIN.getId().equals(targetUser.getUserTypeId().getUserTypeId());

            if (currentIsAdmin == requestDto.getIsAdmin()) {
                log.warn("⚠️ 동일한 권한으로 변경 시도 - 사용자 ID: {}, 현재 권한: {}",
                        userId, currentIsAdmin ? "관리자" : "일반사용자");
                throw new IllegalArgumentException("이미 " + (currentIsAdmin ? "관리자" : "일반사용자") + " 권한을 가지고 있습니다.");
            }

            // 5. 새로운 UserType 조회
            Long newUserTypeId = requestDto.getIsAdmin() ?
                    UserTypeEnum.ADMIN.getId() : UserTypeEnum.USER.getId();

            UserType newUserType = userTypeRepository.findById(newUserTypeId)
                    .orElseThrow(() -> {
                        log.error("❌ UserType을 찾을 수 없음 - UserType ID: {}", newUserTypeId);
                        return new IllegalArgumentException("유효하지 않은 사용자 타입입니다: " + newUserTypeId);
                    });

            // 6. UserType 설정 및 수정 시간 업데이트
            targetUser.setUserTypeId(newUserType);
            targetUser.setModifiedAt(LocalDateTime.now());

            // 7. 저장
            User updatedUser = userRepository.save(targetUser);

            // 8. 저장 후 검증
            boolean verificationIsAdmin = updatedUser.getUserTypeId() != null &&
                    UserTypeEnum.ADMIN.getId().equals(updatedUser.getUserTypeId().getUserTypeId());

            if (verificationIsAdmin != requestDto.getIsAdmin()) {
                log.error("❌ 권한 변경 실패 - 요청된 권한: {}, 실제 권한: {}",
                        requestDto.getIsAdmin() ? "관리자" : "일반사용자",
                        verificationIsAdmin ? "관리자" : "일반사용자");
                throw new RuntimeException("권한 변경에 실패했습니다. 데이터 저장 오류가 발생했습니다.");
            }

            // 9. 응답 DTO 생성
            UserManagementResponseDto responseDto = UserManagementResponseDto.fromEntity(updatedUser);
            return new SingleResponseDto<>(responseDto);

        } catch (Exception e) {
            log.error("❌ 사용자 권한 변경 실패 - 사용자 ID: {}", userId, e);
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("권한 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 권한별 사용자 목록 조회
     */
    public SingleResponseDto<Page<UserManagementResponseDto>> getUsersByRole(boolean isAdmin, Pageable pageable) {
        adminPermissionService.requireAdminPermission("권한별 사용자 조회");

        try {

            Long targetUserTypeId = isAdmin ? UserTypeEnum.ADMIN.getId() : UserTypeEnum.USER.getId();
            Page<User> users = userRepository.findByUserTypeIdAndNotWithdrawn(targetUserTypeId, pageable);

            Page<UserManagementResponseDto> userDtos = users.map(UserManagementResponseDto::fromEntity);

            return new SingleResponseDto<>(userDtos);

        } catch (Exception e) {
            log.error("❌ 권한별 사용자 목록 조회 실패 - 권한: {}", isAdmin ? "관리자" : "일반사용자", e);
            throw new RuntimeException("사용자 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 검색
     */
    public SingleResponseDto<Page<UserManagementResponseDto>> searchUsers(String keyword, Pageable pageable) {
        adminPermissionService.requireAdminPermission("사용자 검색");

        try {

            Page<User> users = userRepository.searchActiveUsers(keyword, pageable);
            Page<UserManagementResponseDto> userDtos = users.map(UserManagementResponseDto::fromEntity);

            return new SingleResponseDto<>(userDtos);

        } catch (Exception e) {
            log.error("❌ 사용자 검색 실패 - 키워드: {}", keyword, e);
            throw new RuntimeException("사용자 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 통계 정보 조회
     */
    public SingleResponseDto<Map<String, Object>> getUserStatistics() {
        adminPermissionService.requireAdminPermission("사용자 통계 조회");

        try {

            // 전체 사용자 수 (탈퇴자 제외)
            long totalUsers = userRepository.countActiveUsers();

            // 관리자 수 (UserTypeEnum.ADMIN)
            long adminCount = userRepository.countByUserTypeIdAndNotWithdrawn(UserTypeEnum.ADMIN.getId());

            // 일반사용자 수 (UserTypeEnum.USER)
            long userCount = userRepository.countByUserTypeIdAndNotWithdrawn(UserTypeEnum.USER.getId());

            // 활성 사용자 수 (탈퇴하지 않은 사용자)
            long activeUserCount = userRepository.countActiveUsers();

            // 소셜 로그인 사용자 수
            long socialLoginCount = userRepository.countSocialLoginUsers();

            Map<String, Object> statistics = Map.of(
                    "totalUsers", totalUsers,
                    "adminCount", adminCount,
                    "userCount", userCount,
                    "activeUserCount", activeUserCount,
                    "socialLoginCount", socialLoginCount
            );


            return new SingleResponseDto<>(statistics);

        } catch (Exception e) {
            log.error("❌ 사용자 통계 정보 조회 실패", e);
            throw new RuntimeException("통계 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}