package com.luti.board.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 후기 목록 조회용 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ReviewListDto {
    /** 후기 고유번호 */
    private final Long reviewId;

    /** 후기 제목 */
    private final String title;

    /** 생성일 */
    private final LocalDateTime createdAt;

    /** 작성자 이름 */
    private final String userName;

    /** 현재 로그인 사용자가 좋아요했는지 여부 */
    private final boolean liked;

    /** 전체 좋아요 수 */
    private final int likeCount;
    
    /** 게시물 조회수 */
    private final int     viewCount;
    
    /** 썸네일 이미지 경로 */
    private final String thumbnailPath;
    
}
