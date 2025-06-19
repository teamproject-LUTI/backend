package com.luti.management.service;

import com.luti.dto.SingleResponseDto;
import com.luti.management.dto.NavigationMenuRequestDto;
import com.luti.management.dto.NavigationMenuResponseDto;
import com.luti.management.entity.NavigationMenu;
import com.luti.management.repository.NavigationMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

            List<NavigationMenu> topLevelMenus = getTopLevelMenus(isAdmin, currentUserTypeId);

            List<NavigationMenuResponseDto> menuTree = topLevelMenus.stream()
                    .map(menu -> buildMenuWithChildren(menu, isAdmin, currentUserTypeId))
                    .collect(Collectors.toList());

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

            List<NavigationMenu> topLevelMenus = menuRepository.findAllByOrderByMenuOrderAsc();

            List<NavigationMenuResponseDto> menuTree = topLevelMenus.stream()
                    .map(menu -> buildAdminMenuWithChildren(menu))
                    .collect(Collectors.toList());

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


    /**
     * 메뉴 생성 (CREATE)
     */
    @Transactional
    public SingleResponseDto<NavigationMenuResponseDto> createMenu(NavigationMenuRequestDto requestDto) {
        adminPermissionService.requireAdminPermission("메뉴 생성");

        try {

            // 입력 데이터 정리
            requestDto.sanitize();

            // 중복 검사
            validateMenuName(requestDto.getName(), requestDto.getParentId(), null);

            // 부모 메뉴 검증
            validateParentMenu(requestDto.getParentId());

            // 메뉴 순서 자동 설정
            if (requestDto.getMenuOrder() == null || requestDto.getMenuOrder() <= 0) {
                requestDto.setMenuOrder(getNextMenuOrder(requestDto.getParentId()));
            }

            // 레벨 계산
            Integer level = calculateMenuLevel(requestDto.getParentId());

            // 현재 사용자 ID 조회
            Long currentUserId = adminPermissionService.getCurrentUserId();

            // 엔티티 생성
            NavigationMenu menu = new NavigationMenu();
            menu.setName(requestDto.getName());
            menu.setDescription(requestDto.getDescription());
            menu.setUrl(requestDto.getUrl());
            menu.setIcon(requestDto.getIcon());
            menu.setMenuOrder(requestDto.getMenuOrder());
            menu.setParentId(requestDto.getParentId());
            menu.setLevel(level);
            menu.setIsActive(requestDto.getIsActive());
            menu.setRequiredRole(requestDto.getRequiredRole()); // DTO에서 권한 레벨 설정
            menu.setHasChildren(false);
            menu.setCreatedAt(LocalDateTime.now());
            menu.setUpdatedAt(LocalDateTime.now());
            menu.setCreatedBy(currentUserId);
            menu.setUpdatedBy(currentUserId);

            // 저장
            NavigationMenu savedMenu = menuRepository.save(menu);

            // 부모 메뉴의 hasChildren 업데이트
            updateParentHasChildren(requestDto.getParentId());

            NavigationMenuResponseDto responseDto = NavigationMenuResponseDto.forAdmin(savedMenu);
            return new SingleResponseDto<>(responseDto);

        } catch (Exception e) {
            log.error("❌ 메뉴 생성 실패 - 메뉴명: {}", requestDto.getName(), e);
            throw new RuntimeException("메뉴 생성 중 오류가 발생했습니다: " + e.getMessage());
        }

    }

    /**
     * 메뉴명 중복 검사
     */
    private void validateMenuName(String name, Long parentId, Long excludeId) {
        List<NavigationMenu> siblings = menuRepository.findByParentIdOrderByMenuOrderAsc(parentId);

        boolean isDuplicate = siblings.stream()
                .filter(menu -> excludeId == null || !menu.getNavigationMenuId().equals(excludeId))
                .anyMatch(menu -> menu.getName().equals(name));

        if (isDuplicate) {
            throw new IllegalArgumentException("같은 레벨에 동일한 메뉴명이 이미 존재합니다: " + name);
        }
    }

    /**
     * 부모 메뉴 검증
     */
    private void validateParentMenu(Long parentId) {
        if (parentId != null && !menuRepository.existsById(parentId)) {
            throw new IllegalArgumentException("부모 메뉴를 찾을 수 없습니다. ID: " + parentId);
        }
    }

    /**
     * 다음 메뉴 순서 계산
     */
    private Integer getNextMenuOrder(Long parentId) {
        List<NavigationMenu> siblings = menuRepository.findByParentIdOrderByMenuOrderAsc(parentId);

        if (siblings.isEmpty()) {
            return 1;
        }

        return siblings.stream()
                .mapToInt(NavigationMenu::getMenuOrder)
                .max()
                .orElse(0) + 1;
    }

    /**
     * 메뉴 레벨 계산
     */
    private Integer calculateMenuLevel(Long parentId) {
        if (parentId == null) {
            return 0;
        }

        NavigationMenu parent = menuRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 메뉴를 찾을 수 없습니다. ID: " + parentId));

        return parent.getLevel() + 1;
    }

    /**
     * 부모 메뉴의 hasChildren 상태 업데이트
     */
    private void updateParentHasChildren(Long parentId) {
        if (parentId != null) {
            NavigationMenu parent = menuRepository.findById(parentId).orElse(null);
            if (parent != null) {
                List<NavigationMenu> children = menuRepository.findByParentIdOrderByMenuOrderAsc(parentId);
                boolean hasChildren = !children.isEmpty();

                if (parent.getHasChildren() != hasChildren) {
                    parent.setHasChildren(hasChildren);
                    parent.setUpdatedAt(LocalDateTime.now());
                    parent.setUpdatedBy(adminPermissionService.getCurrentUserId());
                    menuRepository.save(parent);
                }
            }
        }
    }

    /**
     * 메뉴 수정 (UPDATE)
     */
    @Transactional
    public SingleResponseDto<NavigationMenuResponseDto> updateMenu(Long id, NavigationMenuRequestDto requestDto) {
        adminPermissionService.requireAdminPermission("메뉴 수정");

        try {

            // 기존 메뉴 조회
            NavigationMenu existingMenu = menuRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다. ID: " + id));

            // 입력 데이터 정리
            requestDto.sanitize();

            // 메뉴명 중복 검사 (자기 자신 제외)
            if (!existingMenu.getName().equals(requestDto.getName())) {
                validateMenuName(requestDto.getName(), requestDto.getParentId(), id);
            }

            // 부모 메뉴 변경 검증
            validateParentMenuForUpdate(id, requestDto.getParentId());

            Long oldParentId = existingMenu.getParentId();
            Integer newLevel = calculateMenuLevel(requestDto.getParentId());
            Long currentUserId = adminPermissionService.getCurrentUserId();

            // 엔티티 수정
            existingMenu.setName(requestDto.getName());
            existingMenu.setDescription(requestDto.getDescription());
            existingMenu.setUrl(requestDto.getUrl());
            existingMenu.setIcon(requestDto.getIcon());
            existingMenu.setMenuOrder(requestDto.getMenuOrder());
            existingMenu.setParentId(requestDto.getParentId());
            existingMenu.setLevel(newLevel);
            existingMenu.setIsActive(requestDto.getIsActive());
            existingMenu.setRequiredRole(requestDto.getRequiredRole());
            existingMenu.setUpdatedAt(LocalDateTime.now());
            existingMenu.setUpdatedBy(currentUserId);

            // 저장
            NavigationMenu updatedMenu = menuRepository.save(existingMenu);

            // 부모가 변경된 경우 hasChildren 업데이트
            if (!java.util.Objects.equals(oldParentId, requestDto.getParentId())) {
                updateParentHasChildren(oldParentId);
                updateParentHasChildren(requestDto.getParentId());
            }


            NavigationMenuResponseDto responseDto = NavigationMenuResponseDto.forAdmin(updatedMenu);
            return new SingleResponseDto<>(responseDto);

        } catch (Exception e) {
            log.error("❌ 메뉴 수정 실패 - ID: {}", id, e);
            throw new RuntimeException("메뉴 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 부모 메뉴 변경 검증 (수정 시)
     */
    private void validateParentMenuForUpdate(Long menuId, Long newParentId) {
        if (newParentId != null) {
            // 부모 메뉴 존재 확인
            if (!menuRepository.existsById(newParentId)) {
                throw new IllegalArgumentException("부모 메뉴를 찾을 수 없습니다. ID: " + newParentId);
            }

            // 자기 자신을 부모로 설정하는 것 방지
            if (newParentId.equals(menuId)) {
                throw new IllegalArgumentException("자기 자신을 부모 메뉴로 설정할 수 없습니다.");
            }

            // 순환 참조 방지 (자식 메뉴를 부모로 설정하는 것 방지)
            if (isDescendant(menuId, newParentId)) {
                throw new IllegalArgumentException("하위 메뉴를 부모 메뉴로 설정할 수 없습니다.");
            }
        }
    }

    /**
     * 순환 참조 검사 (targetId가 ancestorId의 후손인지 확인)
     */
    private boolean isDescendant(Long ancestorId, Long targetId) {
        List<NavigationMenu> children = menuRepository.findByParentIdOrderByMenuOrderAsc(ancestorId);

        for (NavigationMenu child : children) {
            if (child.getNavigationMenuId().equals(targetId)) {
                return true;
            }
            if (isDescendant(child.getNavigationMenuId(), targetId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 메뉴 삭제 (DELETE)
     */
    @Transactional
    public SingleResponseDto<String> deleteMenu(Long id) {
        adminPermissionService.requireAdminPermission("메뉴 삭제");

        try {

            // 기존 메뉴 조회
            NavigationMenu menu = menuRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다. ID: " + id));

            // 하위 메뉴가 있는지 확인
            List<NavigationMenu> childMenus = menuRepository.findByParentIdOrderByMenuOrderAsc(id);
            if (!childMenus.isEmpty()) {
                // 하위 메뉴들 먼저 삭제 (재귀적)
                for (NavigationMenu child : childMenus) {
                    deleteMenuRecursively(child.getNavigationMenuId());
                }
            }

            Long parentId = menu.getParentId();
            String menuName = menu.getName();

            // 메뉴 삭제
            menuRepository.delete(menu);

            // 부모 메뉴의 hasChildren 업데이트
            updateParentHasChildren(parentId);

            return new SingleResponseDto<>("메뉴가 성공적으로 삭제되었습니다: " + menuName);

        } catch (Exception e) {
            log.error("❌ 메뉴 삭제 실패 - ID: {}", id, e);
            throw new RuntimeException("메뉴 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 메뉴를 재귀적으로 삭제
     */
    private void deleteMenuRecursively(Long menuId) {
        List<NavigationMenu> children = menuRepository.findByParentIdOrderByMenuOrderAsc(menuId);

        // 하위 메뉴들 먼저 삭제
        for (NavigationMenu child : children) {
            deleteMenuRecursively(child.getNavigationMenuId());
        }

        // 자신 삭제
        menuRepository.deleteById(menuId);
    }
}