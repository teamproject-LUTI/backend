package com.luti.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {
    /** 글쓰기 및 수정할 때 입력해야하는 값 */

    /** 제목 */
    @NotBlank
    private String title;

    /** 내용 */
    @NotBlank
    private String content;

    /** 여행지역 */
    private String travelRegion;

    /** 여행기간 */
    private String travelPeriod;

    /** 장소명 */
    private String spot;

    /** 소요 시간 */
    private String duration;

    /** 예상 비용 */
    private String budget;
    
    /** 이동 방법 */
    private String route;
}
