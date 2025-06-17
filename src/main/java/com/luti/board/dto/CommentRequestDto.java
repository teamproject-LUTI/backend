package com.luti.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 댓글 등록 요청용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CommentRequestDto {
    /** 댓글 내용 */
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;
}
