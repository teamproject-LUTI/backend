package com.luti.menuManagement.repository;

import com.luti.menuManagement.entity.NavigationMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NavigationMenuRepository extends JpaRepository<NavigationMenu, Long> {

    // === 공개용 메뉴 조회 메서드 ===
    /**
     * 최상위 메뉴 조회 (부모가 없는 메뉴, 활성화된 메뉴만)
     */
    List<NavigationMenu> findByParentIdIsNullAndIsActiveTrueOrderByMenuOrderAsc();

    /**
     * 부모 메뉴별 자식 메뉴 조회 (활성화된 메뉴만)
     */
    List<NavigationMenu> findByParentIdAndIsActiveTrueOrderByMenuOrderAsc(Long parentId);

    // === 권한 기반 조회 메서드 (일반 사용자용) ===
    /**
     * 사용자 권한에 따른 최상위 메뉴 조회 (활성화된 메뉴만)
     * requiredRole <= userTypeId 인 메뉴만 조회 (사용자가 접근 가능한 메뉴)
     */
    List<NavigationMenu> findByParentIdIsNullAndIsActiveTrueAndRequiredRoleLessThanEqualOrderByMenuOrderAsc(Integer userTypeId);

    /**
     * 사용자 권한에 따른 자식 메뉴 조회 (활성화된 메뉴만)
     * requiredRole <= userTypeId 인 메뉴만 조회
     */
    List<NavigationMenu> findByParentIdAndIsActiveTrueAndRequiredRoleLessThanEqualOrderByMenuOrderAsc(Long parentId, Integer userTypeId);

    // === 관리자용 조회 메서드 ===
    /**
     * 최상위 메뉴 조회 (모든 상태, 관리자용)
     */
    List<NavigationMenu> findByParentIdIsNullOrderByMenuOrderAsc();

    /**
     * 특정 부모의 모든 자식 메뉴 조회 (비활성화 포함, 관리자용)
     */
    List<NavigationMenu> findByParentIdOrderByMenuOrderAsc(Long parentId);

    /**
     * 비활성화된 메뉴만 조회 (관리용)
     */
    List<NavigationMenu> findByIsActiveFalseOrderByMenuOrderAsc();

    // === 페이징 지원 메서드 (필요시 사용) ===
    /**
     * 활성화된 최상위 메뉴 페이징 조회
     */
    Page<NavigationMenu> findByParentIdIsNullAndIsActiveTrueOrderByMenuOrderAsc(Pageable pageable);
}