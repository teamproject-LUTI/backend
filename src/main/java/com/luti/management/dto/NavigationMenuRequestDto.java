// NavigationMenuRequestDto.java - requiredRole 필드 추가

package com.luti.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationMenuRequestDto {

    @NotBlank(message = "메뉴명은 필수 입니다.")
    @Size(max = 100, message = "메뉴명은 최대 100자까지 입력 가능합니다.")
    private String name;

    @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다.")
    private String description;

    @Min(value = 0, message = "메뉴 순서는 0 이상이어야 합니다.")
    private Integer menuOrder;

    private Long parentId;

    @NotNull(message = "활성화 상태는 필수 입니다.")
    private Boolean isActive = true;

    @Size(max = 500, message = "url 은 500자를 초과할 수 없습니다.")
    @Pattern(regexp = "^(/[\\w\\-._~!$&'()*+,;=:@]*)*/?$",message = "유효한 URL 형식이 아닙니다.")
    private String url;

    @Size(max = 100, message = "아이콘은 100자를 초과할 수 없습니다.")
    private String icon;

    // 🔥 권한 관련 필드 추가
    @Min(value = 1, message = "권한 레벨은 1 이상이어야 합니다.")
    @Max(value = 2, message = "권한 레벨은 2 이하여야 합니다.")
    @NotNull(message = "권한 레벨은 필수입니다.")
    private Integer requiredRole = 1; // 기본값: 일반사용자(1), 관리자(2)

    /**
     * 입력 데이터 정리 메서드
     */
    public void sanitize(){
        if(this.name != null) {
            this.name = this.name.trim();
        }
        if(this.description != null) {
            this.description = this.description.trim();
        }
        if(this.url != null) {
            this.url = this.url.trim();
            if(!this.url.isEmpty() && !this.url.startsWith("/")) {
                this.url = "/" + this.url;
            }
        }

        if(this.icon != null) {
            this.icon = this.icon.trim();
        }

        if(this.requiredRole == null) {
            this.requiredRole = 1;
        }
    }
}