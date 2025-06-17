package com.luti.board.dto;

import com.luti.board.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 댓글 응답용 DTO
 */
@Getter
@AllArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * Entity → DTO 변환
     */
    public static CommentResponseDto of(Comment c) {
        return new CommentResponseDto(
                c.getCommentId(),
                c.getUser().getUserId(),
                c.getContent(),
                c.getCreatedAt(),
                c.getModifiedAt()
        );
    }
}
