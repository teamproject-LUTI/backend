package com.luti.menuManagement.service;

import com.luti.dto.SingleResponseDto;
import com.luti.menuManagement.dto.NavigationMenuResponseDto;
import com.luti.menuManagement.entity.NavigationMenu;
import com.luti.menuManagement.repository.NavigationMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NavigationMenuService {

    private final NavigationMenuRepository menuRepository;
    private final AdminPermissionService adminPermissionService;

    /**
     * 권한에 따른 계층형 메뉴 조회
     * 관리자: 모든 활성화된 메뉴
     * 일반사용자: 권한에 맞는 활성화된 메뉴만
     */
    public SingleResponseDto<List<NavigationMenuResponseDto>> getHierarchicalMenus() {
        try {
            Integer currentUserTypeId = adminPermissionService.getCurrentUserTypeId();
            boolean isAdmin = adminPermissionService.isCurrentUserAdmin();

            log.info("🔍 권한별 메뉴 조회 - 관리자: {}, 사용자타입: {}", isAdmin, currentUserTypeId);

            List<NavigationMenu> topLevelMenus = getTopLevelMenus(isAdmin, currentUserTypeId);

            List<NavigationMenuResponseDto> menuTree = topLevelMenus.stream()
                    .map(menu -> buildMenuWithChildren(menu, isAdmin, currentUserTypeId))
                    .collect(Collectors.toList());

            log.info("✅ 권한별 메뉴 조회 완료 - 총 메뉴 트리: {}", menuTree.size());
            return new SingleResponseDto<>(menuTree);

        } catch (Exception e) {
            log.error("❌ 권한별 메뉴 조회 실패", e);
            throw new RuntimeException("메뉴 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 관리자용 - 모든 메뉴 조회 (비활성화 포함)
     */
    public SingleResponseDto<List<NavigationMenuResponseDto>> getAllMenusForAdmin() {
        adminPermissionService.requireAdminPermission("메뉴 관리");

        try {
            log.info("👑 관리자용 전체 메뉴 조회");

            List<NavigationMenu> topLevelMenus = menuRepository.findByParentIdIsNullOrderByMenuOrderAsc();

            List<NavigationMenuResponseDto> menuTree = topLevelMenus.stream()
                    .map(menu -> buildAdminMenuWithChildren(menu))
                    .collect(Collectors.toList());

            log.info("✅ 관리자용 전체 메뉴 조회 완료 - 총 메뉴: {}", menuTree.size());
            return new SingleResponseDto<>(menuTree);

        } catch (Exception e) {
            log.error("❌ 관리자용 메뉴 조회 실패", e);
            throw new RuntimeException("관리용 메뉴 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 최상위 메뉴 조회 (권한별)
     */
    private List<NavigationMenu> getTopLevelMenus(boolean isAdmin, Integer userTypeId) {
        if (isAdmin) {
            return menuRepository.findByParentIdIsNullAndIsActiveTrueOrderByMenuOrderAsc();
        } else {
            return menuRepository.findByParentIdIsNullAndIsActiveTrueAndRequiredRoleLessThanEqualOrderByMenuOrderAsc(userTypeId);
        }
    }

    /**
     * 메뉴와 자식 메뉴들을 재귀적으로 구성 (권한별)
     */
    private NavigationMenuResponseDto buildMenuWithChildren(NavigationMenu menu, boolean isAdmin, Integer userTypeId) {
        NavigationMenuResponseDto dto = isAdmin ?
                NavigationMenuResponseDto.forAdmin(menu) :
                NavigationMenuResponseDto.forUser(menu);

        List<NavigationMenu> childMenus = getChildMenus(menu.getNavigationMenuId(), isAdmin, userTypeId);

        List<NavigationMenuResponseDto> children = childMenus.stream()
                .map(child -> buildMenuWithChildren(child, isAdmin, userTypeId))
                .collect(Collectors.toList());

        dto.setChildren(children);
        return dto;
    }

    /**
     * 관리자용 메뉴와 자식 메뉴들을 재귀적으로 구성 (비활성화 포함)
     */
    private NavigationMenuResponseDto buildAdminMenuWithChildren(NavigationMenu menu) {
        NavigationMenuResponseDto dto = NavigationMenuResponseDto.forAdmin(menu);

        List<NavigationMenu> childMenus = menuRepository.findByParentIdOrderByMenuOrderAsc(menu.getNavigationMenuId());

        List<NavigationMenuResponseDto> children = childMenus.stream()
                .map(this::buildAdminMenuWithChildren)
                .collect(Collectors.toList());

        dto.setChildren(children);
        return dto;
    }

    /**
     * 자식 메뉴 조회 (권한별)
     */
    private List<NavigationMenu> getChildMenus(Long parentId, boolean isAdmin, Integer userTypeId) {
        if (isAdmin) {
            return menuRepository.findByParentIdAndIsActiveTrueOrderByMenuOrderAsc(parentId);
        } else {
            return menuRepository.findByParentIdAndIsActiveTrueAndRequiredRoleLessThanEqualOrderByMenuOrderAsc(parentId, userTypeId);
        }
    }
}