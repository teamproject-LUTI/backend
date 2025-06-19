package com.luti.board.dto;

import com.luti.board.entity.ReviewAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 리뷰 첨부파일 응답용 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class ReviewAttachmentResponseDto {
    private Long fileNo;
    private String fileName;
    private String physicalPath;
    private String logicalPath;
    private String extension;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ReviewAttachmentResponseDto fromEntity(ReviewAttachment r) {
        return new ReviewAttachmentResponseDto(
                r.getReviewAttachmentId(),
                r.getFileName(),
                r.getPhysicalPath(),
                r.getLogicalPath(),
                r.getExtension(),
                r.getSize(),
                r.getCreatedAt(),
                r.getModifiedAt()
        );
    }
}
