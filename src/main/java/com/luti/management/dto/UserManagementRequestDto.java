package com.luti.management.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementRequestDto {
    /**
     * 관리자 권한 여부 - 유일한 수정 가능 필드
     * true: 관리자, false: 일반사용자
     */
    private Boolean isAdmin;

    /**
     * 권한 변경용 팩토리 메서드
     */
    public static UserManagementRequestDto forRoleChange(boolean isAdmin) {
        return UserManagementRequestDto.builder()
                .isAdmin(isAdmin)
                .build();
    }
}
