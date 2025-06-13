package com.luti.board.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

/**
 * 공지사항 생성·수정 요청을 처리하는 DTO
 * - 클라이언트에서 넘어오는 제목(title)과 내용(content)을 검증
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class NoticeRequestDto {

    /** 공지사항 제목 (필수) */
    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    private String title;

    /** 공지사항 본문 (필수) */
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    private String content;
}
