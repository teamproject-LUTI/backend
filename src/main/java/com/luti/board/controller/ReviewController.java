package com.luti.board.controller;

import com.luti.board.dto.ReviewListDto;
import com.luti.board.dto.ReviewRequestDto;
import com.luti.board.dto.ReviewResponseDto;
import com.luti.board.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**  전체 목록 조회 */
    @GetMapping
    public ResponseEntity<List<ReviewListDto>> listReview(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(userId));
    }

    /**  게시글 상세 조회 */
    @GetMapping("/{reviewNo}")
    public ResponseEntity<ReviewResponseDto> detailReview(
            @PathVariable Long reviewNo,
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        return ResponseEntity.ok(reviewService.getReviewDetail(reviewNo, userId));
    }

    /**  새 게시글 등록 */
    @PostMapping
    public ResponseEntity<Long> createReview(
            @RequestBody @Valid ReviewRequestDto dto,
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        Long reviewNo = reviewService.createReview(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewNo);
    }

    /**  게시글 수정 */
    @PutMapping("/{reviewNo}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewNo,
            @RequestBody @Valid ReviewRequestDto dto,
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        reviewService.updateReview(reviewNo, dto, userId);
        return ResponseEntity.noContent().build();
    }

    /**  게시글 삭제 */
    @DeleteMapping("/{reviewNo}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewNo,
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        reviewService.deleteReview(reviewNo, userId);
        return ResponseEntity.noContent().build();
    }
}