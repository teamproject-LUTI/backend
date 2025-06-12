package com.luti.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 후기 생성/수정 요청용 DTO
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewRequestDto {

    /** 후기 제목 (1~50자) */
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    /** 후기 본문 (최소 1자) */
    @NotBlank
    private String content;

    /** 여행 지역 (최대 255자) */
    @Size(max = 255)
    private String travelRegion;

    /** 여행 기간 (최대 255자) */
    @Size(max = 255)
    private String travelPeriod;
}
