package com.luti.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponseDto {
    private Long reviewNo;
    private String title;
    private String content;
    private int viewCount;
    private int likeCount;
    private LocalDateTime createdAt;

    private String travelRegion;
    private String travelPeriod;
    private String spot;
    private String duration;
    private String budget;
    private String route;

    private String authorName;
    private boolean liked;

}
