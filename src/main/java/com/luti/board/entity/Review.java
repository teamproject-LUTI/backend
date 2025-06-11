package com.luti.board.entity;

import com.luti.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_no")
    private Long reviewNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String content;

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "like_count")
    private int likeCount = 0;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "del_yn", length = 1)
    private String delYn; // "Y", "N"

    @Column(name = "travel_region", length = 255)
    private String travelRegion;

    @Column(name = "travel_period", length = 255)
    private String travelPeriod;

    @Column(name = "spot", length = 255)
    private String spot;

    @Column(name = "duration" ,length = 255)
    private String duration;

    @Column(name = "budget", length = 255)
    private String budget;

    @Column(name = "route",length = 255)
    private String route;
}
