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

    /**
     * 메뉴 순서 재정렬 (드래그 앤 드롭 API)
     */
    @Transactional
    public SingleResponseDto<String> reorderMenus(Long menuId, Integer newOrder, Long parentId) {
        adminPermissionService.requireAdminPermission("메뉴 순서 변경");

        try {
            NavigationMenu menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다. ID: " + menuId));

            Integer oldOrder = menu.getMenuOrder();
            Long currentParentId = menu.getParentId();

            // 부모 변경과 순서 변경이 동시에 일어나는 경우
            if (!java.util.Objects.equals(currentParentId, parentId)) {
                handleParentChangeWithReorder(menu, parentId, newOrder);
            }
            // 같은 부모 내에서 순서만 변경
            else {
                handleSameParentReorder(menu, newOrder);
            }

            return new SingleResponseDto<>("메뉴 순서가 성공적으로 변경되었습니다.");

        } catch (Exception e) {
            log.error("❌ 메뉴 순서 변경 실패 - 메뉴ID: {}, 새순서: {}", menuId, newOrder, e);
            throw new RuntimeException("메뉴 순서 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    /**
     * 메뉴 삽입을 위한 순서 조정
     */
    /**
     * 부모 변경과 함께 순서 변경 처리
     */
    private void handleParentChangeWithReorder(NavigationMenu menu, Long newParentId, Integer newOrder) {
        Long oldParentId = menu.getParentId();
        Integer oldOrder = menu.getMenuOrder();

        // 1. 기존 위치에서 제거하고 순서 정리
        menuRepository.decrementMenuOrderFrom(oldParentId, oldOrder);

        // 2. 새로운 부모에서 적절한 순서 계산 및 공간 확보
        Integer finalOrder = calculateAndPrepareOrderForNewParent(newParentId, newOrder, menu.getNavigationMenuId());

        // 3. 메뉴 정보 업데이트
        menu.setParentId(newParentId);
        menu.setLevel(calculateMenuLevel(newParentId));
        menu.setMenuOrder(finalOrder);
        menu.setUpdatedAt(LocalDateTime.now());
        menu.setUpdatedBy(adminPermissionService.getCurrentUserId());

        menuRepository.save(menu);

        // 4. hasChildren 업데이트
        updateParentHasChildren(oldParentId);
        updateParentHasChildren(newParentId);

    }

    /**
     * 같은 부모 내에서 순서 변경 처리
     */
    private void handleSameParentReorder(NavigationMenu menu, Integer newOrder) {
        Long parentId = menu.getParentId();
        Integer oldOrder = menu.getMenuOrder();

        if (oldOrder.equals(newOrder)) {
            return;
        }

        // 같은 부모의 형제 메뉴들 조회
        List<NavigationMenu> siblings = menuRepository.findByParentIdOrderByMenuOrderAsc(parentId);

        // 새로운 순서가 유효한 범위인지 확인
        int maxOrder = siblings.size();
        if (newOrder < 1 || newOrder > maxOrder) {
            newOrder = Math.max(1, Math.min(newOrder, maxOrder));
            log.warn("⚠️ 순서 범위 조정 - 요청: {} → 조정: {}", newOrder, newOrder);
        }

        // 순서 재정렬 실행
        reorderSiblingsForMove(siblings, menu, newOrder);

    }

    /**
     * 새로운 부모에서 순서 계산 및 공간 확보
     */
    private Integer calculateAndPrepareOrderForNewParent(Long parentId, Integer requestedOrder, Long excludeMenuId) {
        List<NavigationMenu> siblings = menuRepository.findByParentIdOrderByMenuOrderAsc(parentId);

        // 요청된 순서가 없거나 유효하지 않으면 마지막에 추가
        if (requestedOrder == null || requestedOrder <= 0) {
            return siblings.isEmpty() ? 1 : siblings.get(siblings.size() - 1).getMenuOrder() + 1;
        }

        // 요청된 순서가 기존 메뉴들보다 크면 마지막에 추가
        if (requestedOrder > siblings.size()) {
            return siblings.isEmpty() ? 1 : siblings.get(siblings.size() - 1).getMenuOrder() + 1;
        }

        // 요청된 위치부터 뒤의 메뉴들을 1씩 뒤로 밀기
        menuRepository.shiftMenuOrdersForInsert(parentId, requestedOrder, excludeMenuId);

        return requestedOrder;
    }

    /**
     * 형제 메뉴들의 순서 재정렬 (같은 부모 내 이동)
     */
    private void reorderSiblingsForMove(List<NavigationMenu> siblings, NavigationMenu movedMenu, Integer newOrder) {
        Integer oldOrder = movedMenu.getMenuOrder();

        // 이동 방향에 따라 다른 처리
        if (oldOrder < newOrder) {
            // 뒤로 이동: oldOrder+1 ~ newOrder 사이의 메뉴들을 앞으로 1칸씩
            for (NavigationMenu sibling : siblings) {
                if (sibling.getNavigationMenuId().equals(movedMenu.getNavigationMenuId())) {
                    continue;
                }

                Integer siblingOrder = sibling.getMenuOrder();
                if (siblingOrder > oldOrder && siblingOrder <= newOrder) {
                    sibling.setMenuOrder(siblingOrder - 1);
                    sibling.setUpdatedAt(LocalDateTime.now());
                    sibling.setUpdatedBy(adminPermissionService.getCurrentUserId());
                    menuRepository.save(sibling);
                    log.debug("  📦 {}순서 조정: {} → {}", sibling.getName(), siblingOrder, siblingOrder - 1);
                }
            }
        } else {
            // 앞으로 이동: newOrder ~ oldOrder-1 사이의 메뉴들을 뒤로 1칸씩
            for (NavigationMenu sibling : siblings) {
                if (sibling.getNavigationMenuId().equals(movedMenu.getNavigationMenuId())) {
                    continue;
                }

                Integer siblingOrder = sibling.getMenuOrder();
                if (siblingOrder >= newOrder && siblingOrder < oldOrder) {
                    sibling.setMenuOrder(siblingOrder + 1);
                    sibling.setUpdatedAt(LocalDateTime.now());
                    sibling.setUpdatedBy(adminPermissionService.getCurrentUserId());
                    menuRepository.save(sibling);
                    log.debug("  📦 {}순서 조정: {} → {}", sibling.getName(), siblingOrder, siblingOrder + 1);
                }
            }
        }

        // 마지막으로 이동된 메뉴의 순서 업데이트
        movedMenu.setMenuOrder(newOrder);
        movedMenu.setUpdatedAt(LocalDateTime.now());
        movedMenu.setUpdatedBy(adminPermissionService.getCurrentUserId());
        menuRepository.save(movedMenu);

    }

}