package com.luti.board.dto;

import com.luti.board.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Notice 엔티티 응답용 DTO
 */
@Getter
@AllArgsConstructor
public class NoticeResponseDto {
    /** 공지글 고유번호 */
    private Long noticeNo;

    /** 작성자 userId */
    private Long userId;

    /** 제목 */
    private String title;

    /** 내용 */
    private String content;

    /** 조회수 */
    private Integer viewCount;

    /** 삭제 여부 */
    private Boolean deleted;

    /** 생성 시각 */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime modifiedAt;

    public static NoticeResponseDto of(Notice n) {
        return new NoticeResponseDto(
                n.getNoticeNo(),
                n.getUser().getUserId(),      // User 엔티티의 PK getter (getId())
                n.getTitle(),
                n.getContent(),
                n.getViewCount(),
                n.getDeleted(),
                n.getCreatedAt(),
                n.getModifiedAt()
        );
    }
}
