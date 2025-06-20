// src/main/java/com/luti/board/dto/NoticeResponseDto.java
package com.luti.board.dto;

import com.luti.board.entity.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 응답 DTO
 */
@Getter
@Builder
public class NoticeResponseDto {

    private final Long noticeId;
    private final Long userId;
    private final String title;
    private final String content;
    private final Integer viewCount;
    private final Boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Boolean owner;

    /**
     * userId 포함 변환 (owner = true/false)
     */
    public static NoticeResponseDto of(Notice notice, Long currentUserId) {
        boolean isOwner = currentUserId != null &&
                notice.getUser() != null &&
                notice.getUser().getUserId().equals(currentUserId);

        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .userId(notice.getUser().getUserId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .deleted(notice.getDeleted())
                .createdAt(notice.getCreatedAt())
                .modifiedAt(notice.getModifiedAt())
                .owner(isOwner)
                .build();
    }

    /** owner 항상 false */
    public static NoticeResponseDto of(Notice notice) {
        return of(notice, null);
    }
}
