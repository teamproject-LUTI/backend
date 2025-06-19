package com.luti.board.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeResponseDto {

    /** 좋아요를 누른 게시글 번호 */
    private Long reviewId;

    /** 해당 게시글의 현재 좋아요 수 */
    private int likeCount;

    /** 현재 사용자가 좋아요를 눌렀는지 여부 */
    private boolean liked;
}
