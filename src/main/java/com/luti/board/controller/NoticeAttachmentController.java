package com.luti.board.controller;

import com.luti.board.dto.NoticeAttachmentRequestDto;
import com.luti.board.dto.NoticeAttachmentResponseDto;
import com.luti.board.service.NoticeAttachmentService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices/{noticeNo}/attachments")
@RequiredArgsConstructor
public class NoticeAttachmentController {

    private final NoticeAttachmentService service;

    /**
     * 첨부파일 목록 조회
     */
    @GetMapping
    public MultiResponseDto<NoticeAttachmentResponseDto> list(
            @PathVariable Long noticeNo) {
        return service.getAttachments(noticeNo);
    }

    /**
     * 첨부파일 추가
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<NoticeAttachmentResponseDto> create(
            @PathVariable Long noticeNo,
            @RequestBody @Valid NoticeAttachmentRequestDto dto) {
        return service.addAttachment(noticeNo, dto);
    }

    /**
     * 첨부파일 삭제
     */
    @DeleteMapping("/{fileNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long noticeNo,
            @PathVariable Long fileNo) {
        service.deleteAttachment(fileNo);
    }
}
