package com.luti.board.entity;

import com.luti.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE review SET del_yn = 'Y' WHERE review_no = ?")
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
    private String delYn = "N";

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
