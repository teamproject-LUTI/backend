package com.luti.board.dto;

import lombok.*;

/**
 * 공지사항 첨부파일 생성·수정 요청용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class NoticeAttachmentRequestDto {
    @NonNull
    private String fileName;
    @NonNull
    private String physicalPath;
    @NonNull
    private String logicalPath;
    @NonNull
    private String extension;
    @NonNull
    private Long size;
}
