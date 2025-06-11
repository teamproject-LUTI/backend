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
    private String authorName;
    private boolean liked;      // 내가 좋아요 눌렀으면 true
    private int likeCount;
    // 향후 첨부파일 썸네일 경로 등을 추가할 수 있음
    private String thumbnailPath;
}
