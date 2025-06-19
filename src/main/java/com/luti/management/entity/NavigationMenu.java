package com.luti.management.entity;

import com.luti.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "navigation_menu")
public class NavigationMenu {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long navigationMenuId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description; // 설명

    @Column(name = "menu_order" ,nullable = false)
    private Integer menuOrder;  // 메뉴 순서

    @Column(name = "parent_id")
    private Long parentId; // 부모 메뉴 ID (자기 참조 관계)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 메뉴 활성화 여부

    @Column(name = "url", length = 500) // "/admin/users/list" 형태로 계층도 표현
    private String url;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "menu_level", nullable = false)
    private Integer level = 0;

    @Column(name = "has_children", nullable = false)
    private Boolean hasChildren = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // User 엔티티와 연관관계 (생성자)
    @Column(name = "created_by")
    private Long createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdbyUser;

    // User 엔티티와 연관관계 (수정자)
    @Column(name = "updated_by")
    private Long updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private User updatedByUser;

    @Column(name = "required_role", nullable = false)
    private Integer requiredRole = 1; // 기본값: 일반사용자(1), 관리자(2)

    public Integer getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(Integer requiredRole) {
        this.requiredRole = requiredRole;
    }

    // 부모-자식 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private NavigationMenu parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("menuOrder ASC") // 메뉴 순서에 따라 정렬
    private List<NavigationMenu> children = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if(this.menuOrder == null) {
            this.menuOrder = 0; // 기본값 설정
        }

        if(this.isActive == null) {
            this.isActive = true; // 기본값 설정
        }

        if(this.hasChildren == null) {
            this.hasChildren = false; // 기본값 설정
        }

        if(this.level == null) {
            this.level = 0; // 기본값 설정
        }

        // 레벨 계산
        calculateLevel();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateLevel();
    }

    private void calculateLevel() {
        if(this.parentId == null) {
            this.level = 0; // 부모가 없을 경우 기본 레벨 설정
        }else{
            // @PrePersist에서는 parent 관계를 사용할 수 없으므로
            // 서비스 레이어에서 별도 처리 필요
            this.level = 1; // 임시값, 실제로는 서비스에서 계산
        }
    }

    /**
     * 최상위 메뉴인지 확인
     */
    public boolean isTopLevel() {
        return this.parentId == null;
    }

    /**
     * 특정 사용자 타입이 이 메뉴에 접근 가능한지 확인
     * @param userTypeId 사용자의 타입 (1 : 일반 사용자, 2 : 관리자 )
     * @return 접근 가능하면 true, 아니면 false
     */
    public boolean isAccessible(Integer userTypeId) {
        return userTypeId != null && userTypeId >= this.requiredRole;
    }

    /**
     * 권한 레벨 설정 - 일반사용자 이상 접근 가능으로 설정
     */
    public void setPublicAccess() {
        this.requiredRole = 1;
    }

    /**
     * 권한 레벨 설정 - 관리자 전용으로 설정
     */
    public void setAdminOnly() {
        this.requiredRole = 2;
    }
    /**
     * 자식 메뉴 추가
     */
    public void addChild(NavigationMenu child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
        child.setParentId(this.navigationMenuId);
        this.hasChildren = true;
    }

    /**
     * 자식 메뉴 제거 - 메서드명 수정
     */
    public void removeChild(NavigationMenu child) {
        if (this.children != null) {
            this.children.remove(child);
            child.setParent(null);
            child.setParentId(null);

            if (this.children.isEmpty()) {
                this.hasChildren = false;
            }
        }
    }

    /**
     * 생성자 정보 설정
     */
    public void setCreatedBy(Long userId) {
        this.createdBy = userId;
    }

    /**
     * 수정자 정보 설정
     */
    public void setUpdatedBy(Long userId) {
        this.updatedBy = userId;
    }

    /**
     * 생성자 이름 반환
     */
    public String getCreatedByName() {
        return this.createdbyUser != null ? this.createdbyUser.getDisplayName() : null;
    }

    /**
     * 수정자 이름 반환
     */
    public String getUpdatedByName() {
        return this.updatedByUser != null ? this.updatedByUser.getDisplayName() : null;
    }

    /**
     * Builder 패턴을 위한 정적 메서드
     */
    public static NavigationMenu createMenu(String name, String url, String icon, Integer menuOrder, Long createdBy) {
        NavigationMenu menu = new NavigationMenu();
        menu.setName(name);
        menu.setUrl(url);
        menu.setIcon(icon);
        menu.setMenuOrder(menuOrder);
        menu.setCreatedBy(createdBy);
        menu.setUpdatedBy(createdBy);
        return menu;
    }

    /**
     * 서브 메뉴 생성
     */
    public static NavigationMenu createSubMenu(String name, String url, String icon, Integer menuOrder, Long parentId, Long createdBy) {
        NavigationMenu menu = createMenu(name, url, icon, menuOrder, createdBy);
        menu.setParentId(parentId);
        return menu;
    }

    /**
     * 메뉴 정보 업데이트
     */
    public void updateMenu(String name, String description, String url, String icon, Integer menuOrder, Long updatedBy) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.icon = icon;
        this.menuOrder = menuOrder;
        this.updatedBy = updatedBy;
    }

    /**
     * 메뉴 활성화/비활성화
     */
    public void toggleActive(Long updatedBy) {
        this.isActive = !this.isActive;
        this.updatedBy = updatedBy;
    }

}
