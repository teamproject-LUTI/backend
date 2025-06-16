package com.luti.board.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 후기 상세 조회용 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ReviewResponseDto {
    /** 후기 고유번호 */
    private final Long reviewId;

    /** 후기 제목 */
    private final String title;

    /** 후기 본문 */
    private final String content;

    /** 조회수 */
    private final int viewCount;

    /** 전체 좋아요 수 */
    private final int likeCount;

    /** 생성일 */
    private final LocalDateTime createdAt;

    /** 여행 지역 */
    private final String travelRegion;

    /** 여행 기간 */
    private final String travelPeriod;

    /** 작성자 이름 */
    private final String userName;

    /** 현재 로그인 사용자가 좋아요했는지 여부 */
    private final boolean liked;
}
