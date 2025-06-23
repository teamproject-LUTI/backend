package com.luti.management.repository;

import com.luti.management.entity.NavigationMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // === 메뉴 순서 관리 메서드 ===

    /**
     * 같은 부모를 가진 메뉴들 중 특정 순서보다 큰 메뉴들의 순서를 1씩 증가
     */
    @Modifying
    @Query("UPDATE NavigationMenu m SET m.menuOrder = m.menuOrder + 1 WHERE " +
            "(:parentId IS NULL AND m.parentId IS NULL OR m.parentId = :parentId) AND " +
            "m.menuOrder >= :fromOrder AND m.navigationMenuId != :excludeId")
    void incrementMenuOrderFrom(@Param("parentId") Long parentId,
                                @Param("fromOrder") Integer fromOrder,
                                @Param("excludeId") Long excludeId);

    /**
     * 같은 부모를 가진 메뉴들 중 특정 순서보다 큰 메뉴들의 순서를 1씩 감소
     */
    @Modifying
    @Query("UPDATE NavigationMenu m SET m.menuOrder = m.menuOrder - 1 WHERE " +
            "(:parentId IS NULL AND m.parentId IS NULL OR m.parentId = :parentId) AND " +
            "m.menuOrder > :fromOrder")
    void decrementMenuOrderFrom(@Param("parentId") Long parentId,
                                @Param("fromOrder") Integer fromOrder);

    /**
     * 특정 범위의 메뉴 순서를 1씩 증가 (순서 삽입을 위한 공간 확보)
     */
    @Modifying
    @Query("UPDATE NavigationMenu m SET m.menuOrder = m.menuOrder + 1 WHERE " +
            "(:parentId IS NULL AND m.parentId IS NULL OR m.parentId = :parentId) AND " +
            "m.menuOrder >= :insertPosition AND m.navigationMenuId != :excludeId")
    void shiftMenuOrdersForInsert(@Param("parentId") Long parentId,
                                  @Param("insertPosition") Integer insertPosition,
                                  @Param("excludeId") Long excludeId);

    /**
     * 특정 범위의 메뉴 순서를 조정 (드래그 앤 드롭 시 사용)
     */
    @Modifying
    @Query("UPDATE NavigationMenu m SET m.menuOrder = " +
            "CASE " +
            "  WHEN m.navigationMenuId = :movedMenuId THEN :newOrder " +
            "  WHEN :oldOrder < :newOrder AND m.menuOrder > :oldOrder AND m.menuOrder <= :newOrder THEN m.menuOrder - 1 " +
            "  WHEN :oldOrder > :newOrder AND m.menuOrder >= :newOrder AND m.menuOrder < :oldOrder THEN m.menuOrder + 1 " +
            "  ELSE m.menuOrder " +
            "END " +
            "WHERE (:parentId IS NULL AND m.parentId IS NULL OR m.parentId = :parentId)")
    void reorderMenus(@Param("parentId") Long parentId,
                      @Param("movedMenuId") Long movedMenuId,
                      @Param("oldOrder") Integer oldOrder,
                      @Param("newOrder") Integer newOrder);

    /**
     * 같은 부모를 가진 메뉴들 중 특정 순서와 동일한 순서를 가진 메뉴가 있는지 확인
     */
    @Query("SELECT COUNT(m) > 0 FROM NavigationMenu m WHERE " +
            "(:parentId IS NULL AND m.parentId IS NULL OR m.parentId = :parentId) AND " +
            "m.menuOrder = :menuOrder AND (:excludeId IS NULL OR m.navigationMenuId != :excludeId)")
    boolean existsByMenuOrderAndParentId(@Param("parentId") Long parentId,
                                         @Param("menuOrder") Integer menuOrder,
                                         @Param("excludeId") Long excludeId);

    /**
     * 같은 부모의 최상위 메뉴 순서 조회
     */
    @Query("SELECT COALESCE(MIN(m.menuOrder), 1) FROM NavigationMenu m WHERE " +
            "(:parentId IS NULL AND m.parentId IS NULL OR m.parentId = :parentId)")
    Integer findMinMenuOrderByParentId(@Param("parentId") Long parentId);

}

