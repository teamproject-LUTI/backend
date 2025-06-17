package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.ReviewListDto;
import com.luti.board.dto.ReviewRequestDto;
import com.luti.board.dto.ReviewResponseDto;
import com.luti.board.entity.Review;
import com.luti.board.repository.LikeRepository;
import com.luti.board.repository.ReviewRepository;
import com.luti.dto.MultiResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final LikeRepository   likeRepo;
    private final UserRepository   userRepo;

    /** 1. 페이징된 목록 조회 */
    public MultiResponseDto<ReviewListDto> getReviews(int page, int size, Long currentUserId) {
        Page<Review> reviews = reviewRepo.findAll(PageRequest.of(page-1, size));
        List<ReviewListDto> dtos = reviews.stream()
                .map(r -> ReviewListDto.builder()
                        .reviewId(r.getReviewId())
                        .title(r.getTitle())
                        .userName(r.getUser().getNickname())
                        .createdAt(r.getCreatedAt())
                        .likeCount(r.getLikeCount())
                        .liked(likeRepo.existsByReviewReviewIdAndUserUserId(r.getReviewId(), currentUserId))
                        // thumbnailPath에 값을 꼭 넘겨야 합니다!
                        .thumbnailPath(
                                r.getAttachments().isEmpty()
                                        ? null
                                        : r.getAttachments().get(0).getLogicalPath()
                        )
                        .build()
                ).collect(Collectors.toList());

        return new MultiResponseDto<>(dtos, reviews);
    }

    /** 2. 생성 */
    @Transactional
    public Long createReview(ReviewRequestDto req, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Review r = Review.builder()
                .user(user)
                .title(req.getTitle())
                .content(req.getContent())
                .travelRegion(req.getTravelRegion())
                .travelPeriod(req.getTravelPeriod())  // 누락 보완
                .build();
        reviewRepo.save(r);
        return r.getReviewId();
    }

    /** 3. 수정 (작성자만) */
    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto req, Long userId) {
        Review r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));
        if (!r.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Not author");
        }
        r.setTitle(req.getTitle());
        r.setContent(req.getContent());
        r.setTravelRegion(req.getTravelRegion());
        r.setTravelPeriod(req.getTravelPeriod());
    }

    /** 4. 삭제 (soft-delete, 작성자만) */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));
        if (!r.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Not author");
        }
        reviewRepo.delete(r);  // @SQLDelete가 작동합니다
    }

    /** 5. 상세조회 */
    @Transactional
    public ReviewResponseDto getReviewDetail(Long reviewId, Long currentUserId) {
        Review r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));
        r.incrementViewCount();  // 편의 메서드
        // (dirty-checking으로 자동 반영)

        return ReviewResponseDto.builder()
                .reviewId(r.getReviewId())
                .title(r.getTitle())
                .content(r.getContent())
                .viewCount(r.getViewCount())
                .likeCount(r.getLikeCount())
                .createdAt(r.getCreatedAt())
                .travelRegion(r.getTravelRegion())
                .travelPeriod(r.getTravelPeriod())
                .userName(r.getUser().getNickname())
                .liked(likeRepo.existsByReviewReviewIdAndUserUserId(reviewId, currentUserId))
                .build();
    }


    /** 특정 사용자가 작성한 리뷰 총 개수 */
    public long getMyReviewCount(Long userId) {
        return reviewRepo.countByUserUserId(userId);
    }

    /** 특정 사용자가 작성한 모든 리뷰의 조회수 총합 */
    public long getTotalViewCount(Long userId) {
        return reviewRepo.sumViewCountByUserUserId(userId);
    }
}

