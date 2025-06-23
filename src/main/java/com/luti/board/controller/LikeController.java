package com.luti.board.controller;

import com.luti.auth.security.JwtAuthenticationToken;
import com.luti.board.dto.LikeRequestDto;
import com.luti.board.dto.LikeResponseDto;
import com.luti.board.dto.LikedReviewDto;
import com.luti.board.dto.LikedReviewSearchDto;
import com.luti.board.service.LikeService;
import com.luti.dto.MultiResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Slf4j
public class LikeController {
    private final LikeService likeService;

    /** 1. 리뷰 상세에서 좋아요 누르기 */
    @PostMapping
    public ResponseEntity<LikeResponseDto> like(
            @RequestBody LikeRequestDto dto,
            Authentication authentication
    ) {
        // JWT 인증 토큰이 아니라면 401
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // JwtAuthenticationToken 에서 Jwt 꺼내기
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Long userId = token.getCurrentUserId();

        LikeResponseDto resp = likeService.likeReview(dto.getReviewId(), userId);
        return ResponseEntity.ok(resp);
    }

    /** 2. 리뷰 상세에서 좋아요 취소하기 */
    @DeleteMapping
    public ResponseEntity<LikeResponseDto> unlike(
            @RequestBody LikeRequestDto dto,
            Authentication authentication
    ) {
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Long userId = token.getCurrentUserId();

        LikeResponseDto resp = likeService.unlikeReview(dto.getReviewId(), userId);
        return ResponseEntity.ok(resp);
    }

    /** 3-1. 해당 사용자가 좋아요 누른 리뷰 목록(즐겨찾기 페이지) - 기존 방식 (하위 호환) */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LikedReviewDto>> getFavorites(
            @PathVariable Long userId
    ) {
        List<LikedReviewDto> favorites = likeService.getLikedReviews(userId);
        return ResponseEntity.ok(favorites);
    }

    /** 3-2. 페이지네이션과 검색 조건을 적용한 좋아요 누른 리뷰 목록 조회 */
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<MultiResponseDto<LikedReviewDto>> getFavoritesWithPagination(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "") String searchType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("좋아요 누른 리뷰 검색 요청 - 사용자 ID: {}, 검색타입: {}, 키워드: {}, 정렬: {}, 페이지: {}, 크기: {}",
                userId, searchType, keyword, sortBy, page, size);

        // 검색 조건 DTO 생성
        LikedReviewSearchDto searchDto = new LikedReviewSearchDto();
        searchDto.setSearchType(searchType);
        searchDto.setKeyword(keyword);
        searchDto.setSortBy(sortBy);
        searchDto.setPage(page);
        searchDto.setSize(size);

        // 페이지네이션이 적용된 결과 조회
        Page<LikedReviewDto> result = likeService.getLikedReviewsWithPagination(userId, searchDto);

        // MultiResponseDto로 감싸서 반환
        MultiResponseDto<LikedReviewDto> response = new MultiResponseDto<>(result.getContent(), result);

        return ResponseEntity.ok(response);
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

    /** 5. 내가 올린 모든 리뷰에 받은 좋아요 총 개수 조회 */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getTotalReceivedLikes(
            @PathVariable Long userId
    ) {
        long totalLikes = likeService.getTotalReceivedLikes(userId);
        return ResponseEntity.ok(totalLikes);
    }
}
