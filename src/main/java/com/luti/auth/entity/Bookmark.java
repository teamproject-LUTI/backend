package com.luti.auth.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "bookmark")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

    @Id
    @Column(name="plan_no")
    private long planNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id", referencedColumnName = "login_id", insertable = false, updatable = false)
    private User loginId;

    @Column(name = "route_title")
    private String routeTitle;

    @Column(name = "route_content")
    private String routeContent;

}
