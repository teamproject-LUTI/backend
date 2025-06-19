package com.luti.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikedReviewDto {

    /** 즐겨찾기(좋아요 누른 게시물)페이지용 */
    /** 글번호 */
    private Long reviewId;

    /** 글 제목(목록에 표기) */
    private String title;

    /** 작성자 이름(닉네임 우선) */
    private String authorName;

    /** 좋아요 */
    private int likeCount;

    /** 현재 사용자가 좋아요했는지 여부 */
    private boolean liked;

    /** 썸네일 이미지 경로 */
    private String thumbnailPath;

    /** 글 내용 */
    private String content;

    /** 조회수 */
    private int viewCount;

    /** 여행 지역 */
    private String travelRegion;

    /** 여행 기간 */
    private String travelPeriod;


}
