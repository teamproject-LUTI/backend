package com.luti.board.controller;

import com.luti.board.dto.AskRequestDto;
import com.luti.board.dto.AskResponseDto;
import com.luti.board.service.AskService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asks")
@RequiredArgsConstructor
public class AskController {

    private final AskService askService;

    /** 문의글 목록 조회 (페이징) */
    @GetMapping
    public MultiResponseDto<AskResponseDto> listAsks(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return askService.getAsks(page, size, userId);
    }

    /** 단일 문의글 조회 */
    @GetMapping("/{askId}")
    public SingleResponseDto<AskResponseDto> getAsk(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long askId) {
        return askService.getAsk(askId, userId);
    }

    /** 새 문의글 등록 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<AskResponseDto> createAsk(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid AskRequestDto dto) {
        return askService.createAsk(userId, dto);
    }

    /** 문의글 수정 */
    @PatchMapping("/{askId}")
    public SingleResponseDto<AskResponseDto> updateAsk(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long askId,
            @RequestBody @Valid AskRequestDto dto) {
        return askService.updateAsk(askId, dto, userId);
    }

    /** 문의글 삭제 */
    @DeleteMapping("/{askId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsk(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long askId) {
        askService.deleteAsk(askId, userId);
    }
}
