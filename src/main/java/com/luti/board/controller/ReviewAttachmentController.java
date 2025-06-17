package com.luti.board.controller;

import com.luti.board.dto.ReviewAttachmentRequestDto;
import com.luti.board.dto.ReviewAttachmentResponseDto;
import com.luti.board.service.ReviewAttachmentService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    /** 2) 리뷰에 첨부파일 추가 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<ReviewAttachmentResponseDto> addAttachment(
            @PathVariable Long reviewId,
            @RequestBody ReviewAttachmentRequestDto dto) {
        return service.addAttachment(reviewId, dto);
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
