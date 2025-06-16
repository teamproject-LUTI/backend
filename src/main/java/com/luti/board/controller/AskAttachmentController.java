package com.luti.board.controller;

import com.luti.board.dto.AskAttachmentRequestDto;
import com.luti.board.dto.AskAttachmentResponseDto;
import com.luti.board.service.AskAttachmentService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code AskAttachmentController}는 문의글 첨부파일 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/asks/{askId}/attachments")
@RequiredArgsConstructor
public class AskAttachmentController {

    private final AskAttachmentService service;

    /** 첨부파일 등록 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<AskAttachmentResponseDto> create(
            @PathVariable Long askId,
            @RequestBody @Valid AskAttachmentRequestDto dto
    ) {
        Long id = service.saveAttachment(askId, dto);
        AskAttachmentResponseDto resp = service.getAttachment(id);
        return new SingleResponseDto<>(resp);
    }

    /** 첨부파일 목록 조회 */
    @GetMapping
    public MultiResponseDto<AskAttachmentResponseDto> list(
            @PathVariable Long askId
    ) {
        List<AskAttachmentResponseDto> list = service.getAttachmentsByAsk(askId);
        return new MultiResponseDto<>(list, null);
    }

    /** 특정 첨부파일 삭제 */
    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long attachmentId) {
        service.deleteAttachment(attachmentId);
    }
}
