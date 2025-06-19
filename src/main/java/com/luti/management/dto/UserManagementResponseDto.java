package com.luti.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luti.auth.entity.User;
import com.luti.auth.enums.UserTypeEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserManagementResponseDto {
    private Long userId;
    private String name;
    private String email;
    private String nickname;
    private Boolean isAdmin;
    private String userType;
    private Long userTypeId;
    private Boolean isEmailVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private String provider; // 소셜 로그인 제공자
    private Boolean canModify; // 수정 가능 여부 (메인 관리자 보호용)

    /**
     * User 엔티티로부터 DTO 생성 (기본)
     */
    public static UserManagementResponseDto fromEntity(User user) {
        if (user == null) {
            return null;
        }

        // 관리자 여부 판단 (UserTypeEnum.ADMIN 사용)
        boolean isAdmin = user.getUserTypeId() != null &&
                UserTypeEnum.ADMIN.getId().equals(user.getUserTypeId().getUserTypeId());


        return UserManagementResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .isAdmin(isAdmin)
                .userType(isAdmin ? "관리자" : "일반사용자")
                .userTypeId(user.getUserTypeId() != null ? user.getUserTypeId().getUserTypeId() : null)
                .isEmailVerified(true)
                .isActive(!"Y".equals(user.getWithdrawYn()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getModifiedAt())
                .lastLoginAt(null)
                .provider(user.getProvider())
                .canModify(true)
                .build();
    }

    /**
     * 요약 정보용 DTO 생성 (목록용)
     */
    public static UserManagementResponseDto forSummary(User user) {
        return fromEntity(user); // 간단하므로 동일하게 처리
    }

}
