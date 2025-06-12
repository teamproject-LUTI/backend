package com.luti.board.dto;

import com.luti.board.entity.NoticeAttachment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 첨부파일 응답용 DTO
 */
@Getter
@AllArgsConstructor
public class NoticeAttachmentResponseDto {
    private Long fileNo;
    private String fileName;
    private String physicalPath;
    private String logicalPath;
    private String extension;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * Entity → DTO 변환
     */
    public static NoticeAttachmentResponseDto of(NoticeAttachment a) {
        return new NoticeAttachmentResponseDto(
                a.getId(),
                a.getFileName(),
                a.getPhysicalPath(),
                a.getLogicalPath(),
                a.getExtension(),
                a.getSize(),
                a.getCreatedAt(),
                a.getModifiedAt()
        );
    }
}
