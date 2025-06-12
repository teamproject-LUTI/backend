package com.luti.board.controller;

import com.luti.board.dto.NoticeRequestDto;
import com.luti.board.dto.NoticeResponseDto;
import com.luti.board.service.NoticeService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * {@code NoticeController}는 공지사항 게시판 REST API를 제공
 * - 공지사항 등록(Create)
 * - 공지사항 목록 조회(Read, 페이징)
 * - 단일 공지사항 조회(Read)
 * - 공지사항 수정(Update)
 * - 공지사항 삭제(soft delete)
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 등록
     *
     * @param userId 작성자 User의 PK
     * @param dto    공지사항 제목과 내용을 담은 요청 DTO
     * @return 생성된 공지사항 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<NoticeResponseDto> create(
            @RequestParam Long userId,
            @RequestBody @Valid NoticeRequestDto dto) {
        return noticeService.createNotice(userId, dto);
    }

    /**
     * 공지사항 목록 조회 (페이징)
     *
     * @param page 요청 페이지 번호 (1-based, default = 1)
     * @param size 페이지당 항목 수 (default = 10)
     * @return 페이징된 공지사항 목록
     */
    @GetMapping
    public MultiResponseDto<NoticeResponseDto> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return noticeService.getNotices(page, size);
    }

    /**
     * 단일 공지사항 상세 조회
     *
     * @param noticeNo 공지사항 고유번호
     * @return 요청한 공지사항 정보
     */
    @GetMapping("/{noticeNo}")
    public SingleResponseDto<NoticeResponseDto> detail(
            @PathVariable Long noticeNo) {
        return noticeService.getNotice(noticeNo);
    }

    /**
     * 공지사항 수정
     *
     * @param noticeNo 수정할 공지사항 고유번호
     * @param dto      변경할 제목과 내용을 담은 요청 DTO
     * @return 수정된 공지사항 정보
     */
    @PatchMapping("/{noticeNo}")
    public SingleResponseDto<NoticeResponseDto> update(
            @PathVariable Long noticeNo,
            @RequestBody @Valid NoticeRequestDto dto) {
        return noticeService.updateNotice(noticeNo, dto);
    }

    /**
     * 공지사항 삭제 (soft delete)
     *
     * @param noticeNo 삭제할 공지사항 고유번호
     */
    @DeleteMapping("/{noticeNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long noticeNo) {
        noticeService.deleteNotice(noticeNo);
    }
}
