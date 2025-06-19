package com.luti.board.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ReviewAttachmentRequestDto {

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
