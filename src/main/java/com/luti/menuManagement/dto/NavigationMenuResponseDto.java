package com.luti.menuManagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luti.menuManagement.entity.NavigationMenu;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavigationMenuResponseDto {

    private Long navigationMenuId;
    private String name;
    private String description; // 설명
    private Integer menuOrder;  // 메뉴 순서
    private Long parentId; // 부모 메뉴 ID (자기 참조 관계)
    private Boolean isActive = true; // 메뉴 활성화 여부
    private String url; // "/admin/users/list" 형태로 계층도 표현
    private String icon;
    private Integer level;
    private Boolean hasChildren = false; // 자식 메뉴 존재 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자, 수정자 ID
    private Long createdBy;
    private Long updatedBy;

    // 생성자 수정자 이름
    private String createdByName;
    private String updatedByName;

    // 계층 구조를 위한 필드
    private List<NavigationMenuResponseDto> children;

    // 추가 정보 필드
    private String parentMenuName;
    private String parentMenuUrl;

    // 권한 관련 필드
    private Boolean canModify; // 수정 가능 여부
    private Boolean canDelete;

    /**
     * Entity를 받는 생성자 (전체 정보 포함)
     */
    public NavigationMenuResponseDto(NavigationMenu menu) {
        this.navigationMenuId = menu.getNavigationMenuId();
        this.name = menu.getName();
        this.description = menu.getDescription();
        this.menuOrder = menu.getMenuOrder();
        this.parentId = menu.getParentId();
        this.isActive = menu.getIsActive();
        this.url = menu.getUrl();
        this.icon = menu.getIcon();
        this.level = menu.getLevel();
        this.hasChildren = menu.getHasChildren();
        this.createdAt = menu.getCreatedAt();
        this.updatedAt = menu.getUpdatedAt();
        this.createdBy = menu.getCreatedBy();
        this.updatedBy = menu.getUpdatedBy();
        this.children = new ArrayList<>();
    }

    /**
     * 공개용 DTO 생성 (민감한 정보 제외)
     */
    public static NavigationMenuResponseDto forPublic(NavigationMenu menu) {
        NavigationMenuResponseDto dto = new NavigationMenuResponseDto();

        // 공개해도 되는 정보만 설정
        dto.setNavigationMenuId(menu.getNavigationMenuId());
        dto.setName(menu.getName());
        dto.setUrl(menu.getUrl());
        dto.setIcon(menu.getIcon());
        dto.setMenuOrder(menu.getMenuOrder());
        dto.setLevel(menu.getLevel());
        dto.setHasChildren(menu.getHasChildren());
        dto.setParentId(menu.getParentId());
        dto.setIsActive(menu.getIsActive());

        // 권한 관련 정보는 설정하지 않음
        dto.setCanModify(false);
        dto.setCanDelete(false);

        // 빈 자식 리스트 초기화
        dto.setChildren(new ArrayList<>());

        return dto;
    }

    /**
     * 관리자용 DTO 생성 (모든 정보 포함)
     */
    public static NavigationMenuResponseDto forAdmin(NavigationMenu menu) {
        NavigationMenuResponseDto dto = new NavigationMenuResponseDto(menu);

        // 관리자 권한 설정
        dto.setCanModify(true);
        dto.setCanDelete(true);

        return dto;
    }

    /**
     * 일반 사용자용 DTO 생성
     */
    public static NavigationMenuResponseDto forUser(NavigationMenu menu) {
        NavigationMenuResponseDto dto = new NavigationMenuResponseDto(menu);

        // 일반 사용자 권한 설정
        dto.setCanModify(false);
        dto.setCanDelete(false);

        return dto;
    }
}