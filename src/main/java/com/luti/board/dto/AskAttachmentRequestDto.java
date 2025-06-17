package com.luti.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 문의 첨부파일 등록/수정용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class AskAttachmentRequestDto {
    /** 원본 파일명 */
    @NotBlank
    private String fileName;

    /** 실제 저장된 물리 경로 */
    @NotBlank
    private String physicalPath;

    /** 논리 경로(URI) */
    @NotBlank
    private String logicalPath;

    /** 확장자(ex: jpg, png) */
    @NotBlank
    private String extension;

    /** 파일 크기(bytes) */
    @NotNull
    private Long size;
}
