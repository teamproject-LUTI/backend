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
    private String userName;
    private final String title;
    private final String content;
    private final Integer viewCount;
    private final Boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Boolean owner;
    private final Boolean isAdmin;  // 관리자 여부 필드 추가

    /**
     * userId 포함 변환 (owner = true/false, isAdmin 계산)
     */
    public static NoticeResponseDto of(Notice notice, Long userId) {
        boolean isOwner = notice.getUser() != null &&
                notice.getUser().getUserId().equals(userId);

        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .userId(notice.getUser().getUserId())
                .userName(notice.getUser().getName())
                .title(notice.getTitle())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .deleted(notice.getDeleted())
                .createdAt(notice.getCreatedAt())
                .modifiedAt(notice.getModifiedAt())
                .owner(isOwner)
                .isAdmin(false)  // 기본값, Service에서 별도 설정
                .build();
    }

    /**
     * 관리자 여부를 포함한 변환 메서드
     */
    public static NoticeResponseDto of(Notice notice, Long userId, boolean isAdmin) {
        boolean isOwner = notice.getUser() != null &&
                notice.getUser().getUserId().equals(userId);

        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .userId(notice.getUser().getUserId())
                .userName(notice.getUser().getName())
                .title(notice.getTitle())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .deleted(notice.getDeleted())
                .createdAt(notice.getCreatedAt())
                .modifiedAt(notice.getModifiedAt())
                .owner(isOwner)
                .isAdmin(isAdmin)
                .build();
    }

    /** owner 항상 false */
    public static NoticeResponseDto of(Notice notice) {
        return of(notice, null);
    }
}