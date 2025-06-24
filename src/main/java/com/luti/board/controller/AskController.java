package com.luti.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.luti.board.dto.AskRequestDto;
import com.luti.board.dto.AskResponseDto;
import com.luti.board.repository.MyAskSearchDto;
import com.luti.board.service.AskService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/asks")
@RequiredArgsConstructor
@Slf4j
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

	/**
	 * 내 문의글 목록 조회 (정렬 + 답변 상태 필터 적용)
	 */
	@GetMapping("/my")
	public MultiResponseDto<AskResponseDto> getMyAsks(
			@AuthenticationPrincipal Long userId,
			@RequestParam(defaultValue = "latest") String sortBy,
			@RequestParam(defaultValue = "all") String answerStatus,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		log.info("내 문의글 조회 요청 - 사용자 ID: {}, 정렬: {}, 답변상태: {}, 페이지: {}, 크기: {}",
				userId, sortBy, answerStatus, page, size);

		// 검색 조건 DTO 생성
		MyAskSearchDto searchDto = new MyAskSearchDto();
		searchDto.setSortBy(sortBy);
		searchDto.setAnswerStatus(answerStatus);
		searchDto.setPage(page);
		searchDto.setSize(size);

		return askService.getMyAsks(userId, searchDto);
	}

}
