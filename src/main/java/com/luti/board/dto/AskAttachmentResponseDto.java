package com.luti.board.dto;

import com.luti.board.entity.AskAttachment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문의 첨부파일 응답용 DTO
 */
@Getter
@AllArgsConstructor
public class AskAttachmentResponseDto {
    private Long askAttachmentId;
    private String fileName;
    private String physicalPath;
    private String logicalPath;
    private String extension;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static AskAttachmentResponseDto of(AskAttachment a) {
        return new AskAttachmentResponseDto(
                a.getAskAttachmentId(),
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
