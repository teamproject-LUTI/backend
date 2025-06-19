package com.luti.board.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequestDto {

    /** 좋아요 누를 게시글 번호 */
    private Long reviewId;

    /** 좋아요 누르는 사용자의 ID */
    private Long userId;
}
