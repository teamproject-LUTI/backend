package com.luti.menuManagement.repository;

import com.luti.menuManagement.entity.NavigationMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * 모든 메뉴 조회 (관리자용 - 최상위와 하위 메뉴 모두 포함)
     */
    List<NavigationMenu> findAllByOrderByMenuOrderAsc();


    /**
     * 특정 부모의 모든 자식 메뉴 조회 (비활성화 포함, 관리자용)
     */
    List<NavigationMenu> findByParentIdOrderByMenuOrderAsc(Long parentId);

    /**
     * 특정 부모 하위의 최대 메뉴 순서 조회
     */
    @Query("SELECT COALESCE(MAX(m.menuOrder), 0) FROM NavigationMenu m WHERE m.parentId = :parentId")
    Integer findMaxMenuOrderByParentId(@Param("parentId") Long parentId);

    /**
     * 같은 부모를 가진 메뉴들 중 동일한 이름이 있는지 확인 (자기 자신 제외)
     */
    @Query("SELECT COUNT(m) > 0 FROM NavigationMenu m WHERE m.name = :name AND m.parentId = :parentId AND (:excludeId IS NULL OR m.navigationMenuId != :excludeId)")
    boolean existsByNameAndParentIdExcludingId(@Param("name") String name, @Param("parentId") Long parentId, @Param("excludeId") Long excludeId);

    /**
     * 같은 부모를 가진 메뉴들 중 동일한 이름이 있는지 확인
     */
    @Query("SELECT COUNT(m) > 0 FROM NavigationMenu m WHERE m.name = :name AND m.parentId = :parentId")
    boolean existsByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    /**
     * 최상위 메뉴들 중 동일한 이름이 있는지 확인 (parentId가 null인 경우)
     */
    @Query("SELECT COUNT(m) > 0 FROM NavigationMenu m WHERE m.name = :name AND m.parentId IS NULL")
    boolean existsByNameAndParentIdIsNull(@Param("name") String name);
}

