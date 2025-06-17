package com.luti.board.controller;

import com.luti.board.dto.ReviewAttachmentRequestDto;
import com.luti.board.dto.ReviewAttachmentResponseDto;
import com.luti.board.service.ReviewAttachmentService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/{reviewId}/attachments")
@RequiredArgsConstructor
public class ReviewAttachmentController {

    private final ReviewAttachmentService service;

    /** 1) 특정 리뷰의 첨부파일 목록 조회 */
    @GetMapping
    public MultiResponseDto<ReviewAttachmentResponseDto> getAttachments(
            @PathVariable Long reviewId) {
        return service.getAttachments(reviewId);
    }

    /** 2) 리뷰에 첨부파일 추가 (Multipart) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<ReviewAttachmentResponseDto> addAttachments(
            @PathVariable Long reviewId,
            @RequestPart("files") List<MultipartFile> files) {
        // 서비스 계층에서 MultipartFile 리스트 처리하도록 구현
        return service.addAttachmentFiles(reviewId, files);
    }

    /** 3) 첨부파일 삭제  */
    @DeleteMapping("/{fileNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(
            @PathVariable Long reviewId,
            @PathVariable Long fileNo) {
        service.deleteAttachment(fileNo);
    }

}
