package com.luti.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewListDto {
    private Long reviewNo;
    private String title;
    private LocalDateTime createdAt;
    private String userName;
    private boolean liked;
    private int likeCount;
    private String thumbnailPath;
}
