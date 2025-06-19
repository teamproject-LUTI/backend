package com.luti.board.controller;

import com.luti.board.dto.AskRequestDto;
import com.luti.board.dto.AskResponseDto;
import com.luti.board.service.AskService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return askService.getAsks(page, size);
    }

    /** 단일 문의글 조회 */
    @GetMapping("/{askId}")
    public SingleResponseDto<AskResponseDto> getAsk(@PathVariable Long askId) {
        return askService.getAsk(askId);
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
            @PathVariable Long askId,
            @RequestBody @Valid AskRequestDto dto
    ) {
        return askService.updateAsk(askId, dto);
    }

    /** 문의글 삭제 */
    @DeleteMapping("/{askId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsk(@PathVariable Long askId) {
        askService.deleteAsk(askId);
    }
}
