// src/main/java/com/luti/board/controller/NoticeController.java
package com.luti.board.controller;

import com.luti.board.dto.NoticeRequestDto;
import com.luti.board.dto.NoticeResponseDto;
import com.luti.board.service.NoticeService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /** 공지 작성 (관리자만) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<NoticeResponseDto> create(
            @AuthenticationPrincipal Long userId,        // ← JWT 토큰에서 꺼낸 userId
            @RequestBody @Valid NoticeRequestDto dto
    ) {
        return noticeService.createNotice(userId, dto);
    }

    /** 공지 목록 (누구나) */
    @GetMapping
    public MultiResponseDto<NoticeResponseDto> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return noticeService.getNotices(page, size, userId);
    }

    /** 공지 상세 (누구나) */
    @GetMapping("/{noticeId}")
    public SingleResponseDto<NoticeResponseDto> detail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long noticeId) {
        return noticeService.getNotice(noticeId, userId);
    }

    /** 공지 수정 (관리자만) */
    @PatchMapping("/{noticeId}")
    public SingleResponseDto<NoticeResponseDto> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeRequestDto dto
    ) {
        return noticeService.updateNotice(noticeId, dto, userId);
    }

    /** 공지 삭제 (관리자만) */
    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long noticeId
    ) {
        noticeService.deleteNotice(noticeId, userId);
    }
}
