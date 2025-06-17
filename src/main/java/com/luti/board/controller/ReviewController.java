package com.luti.board.controller;

import com.luti.board.dto.ReviewListDto;
import com.luti.board.dto.ReviewRequestDto;
import com.luti.board.dto.ReviewResponseDto;
import com.luti.board.service.ReviewService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @GetMapping
    public MultiResponseDto<ReviewListDto> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal(expression="userId") Long userId
    ) {
        return service.getReviews(page, size, userId);
    }

    @GetMapping("/{reviewId}")
    public SingleResponseDto<ReviewResponseDto> getOne(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal(expression="userId") Long userId
    ) {
        return new SingleResponseDto<>(service.getReviewDetail(reviewId, userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<Long> create(
            @RequestBody @Valid ReviewRequestDto dto,
            @AuthenticationPrincipal(expression="userId") Long userId
    ) {
        return new SingleResponseDto<>(service.createReview(dto, userId));
    }

    @PutMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRequestDto dto,
            @AuthenticationPrincipal(expression="userId") Long userId
    ) {
        service.updateReview(reviewId, dto, userId);
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal(expression="userId") Long userId
    ) {
        service.deleteReview(reviewId, userId);
    }
}
