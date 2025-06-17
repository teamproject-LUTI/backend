package com.luti.board.controller;

import com.luti.board.dto.LikeRequestDto;
import com.luti.board.dto.LikeResponseDto;
import com.luti.board.dto.LikedReviewDto;
import com.luti.board.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    /** 1. 리뷰 상세에서 좋아요 누르기 */
    @PostMapping
    public ResponseEntity<LikeResponseDto> like(
            @RequestBody LikeRequestDto dto,
            @AuthenticationPrincipal Long userId
    ) {
        LikeResponseDto response = likeService.likeReview(
                dto.getReviewId(),
                dto.getUserId()
        );
        return ResponseEntity.ok(response);
    }

    /** 2. 리뷰 상세에서 좋아요 취소하기 */
    @DeleteMapping
    public ResponseEntity<LikeResponseDto> unlike(
            @RequestBody LikeRequestDto dto
    ) {
        LikeResponseDto response = likeService.unlikeReview(
                dto.getReviewId(),
                dto.getUserId()
        );
        return ResponseEntity.ok(response);
    }

    /** 3. 해당 사용자가 좋아요 누른 리뷰 목록(즐겨찾기 페이지) */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LikedReviewDto>> getFavorites(
            @PathVariable Long userId
    ) {
        List<LikedReviewDto> favorites = likeService.getLikedReviews(userId);
        return ResponseEntity.ok(favorites);
    }

    /** 4. 상세·목록 페이지 로딩 시 해당 리뷰에 대한 좋아요 여부 확인 */
    @GetMapping("/{reviewId}/user/{userId}")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable Long reviewId,
            @PathVariable Long userId
    ) {
        boolean liked = likeService.isLiked(reviewId, userId);
        return ResponseEntity.ok(liked);
    }
}
