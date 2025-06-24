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
    private String userName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean isOwner;        // 현재 사용자가 작성자인지 여부

    // 이 메서드가 있는지 확인
    public boolean isOwner() {
        return isOwner;
    }

    /**
     * Entity → DTO 변환 (현재 사용자 ID 포함)
     */
    public static CommentResponseDto of(Comment comment, Long currentUserId) {
        // null 체크를 통한 안전한 비교
        boolean isOwner = false;
        if (currentUserId != null && comment.getUser() != null && comment.getUser().getUserId() != null) {
            isOwner = currentUserId.equals(comment.getUser().getUserId());
        }

        // 사용자 표시명 가져오기 (null 안전)
        String displayName = "익명";
        if (comment.getUser() != null) {
            String userDisplayName = comment.getUser().getDisplayName();
            displayName = (userDisplayName != null && !userDisplayName.trim().isEmpty())
                    ? userDisplayName : "익명";
        }

        return new CommentResponseDto(
                comment.getCommentId(),
                comment.getUser() != null ? comment.getUser().getUserId() : null,
                displayName,
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                isOwner
        );
    }

    /**
     * Entity → DTO 변환 (현재 사용자 ID 없는 경우)
     */
    public static CommentResponseDto of(Comment comment) {
        return of(comment, null);
    }
}