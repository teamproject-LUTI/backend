package com.luti.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 문의글 등록/수정 요청 DTO
 */
@Getter
@Setter
public class AskRequestDto {

    /** 문의글 제목 */
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    /** 문의글 내용 */
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
