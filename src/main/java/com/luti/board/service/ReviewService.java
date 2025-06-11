package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.board.dto.ReviewListDto;
import com.luti.board.dto.ReviewRequestDto;
import com.luti.board.dto.ReviewResponseDto;
import com.luti.board.entity.Review;
import com.luti.board.repository.LikeRepository;
import com.luti.board.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;


    // 게시물 목록 조회
    public List<ReviewListDto> getAllReviews(Long userId){
        return reviewRepository.findAll()
                .stream()
                .map(r -> {
                    ReviewListDto dto = new ReviewListDto();
                    dto.setReviewNo(r.getReviewNo());
                    dto.setTitle(r.getTitle());
                    dto.setCreatedAt(r.getCreatedAt());
                    dto.setAuthorName(r.getUser().getNickname());
                    dto.setLikeCount(r.getLikeCount());
                    dto.setLiked(likeRepository.existsByReview_ReviewNoAndUser_UserId(r.getReviewNo(), userId));

                    return dto;
                }).collect(Collectors.toList());
    }

    // 새 글 등록
    @Transactional
    public Long createReview(ReviewRequestDto req, Long userId) {
        User user = new User();
        user.setUserId(userId);

        Review review = new Review();
        review.setUser(user);
        review.setTitle(req.getTitle());
        review.setContent(req.getContent());
        review.setTravelRegion(req.getTravelRegion());
        review.setTravelPeriod(req.getTravelPeriod());
        review.setSpot(req.getSpot());
        review.setDuration(req.getDuration());
        review.setBudget(req.getBudget());
        review.setRoute(req.getRoute());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
        return review.getReviewNo();
    }

    // 상세 조회 (+조회수 증가)
    @Transactional
    public ReviewResponseDto getReviewDetail(Long reviewNo, Long currentUserId) {
        Review r = reviewRepository.findById(reviewNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. reviewNo=" + reviewNo));

        // 조회수 증가
        r.setViewCount(r.getViewCount() + 1);
        reviewRepository.save(r);

        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setReviewNo(r.getReviewNo());
        dto.setTitle(r.getTitle());
        dto.setContent(r.getContent());
        dto.setViewCount(r.getViewCount());
        dto.setLikeCount(r.getLikeCount());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setTravelRegion(r.getTravelRegion());
        dto.setTravelPeriod(r.getTravelPeriod());
        dto.setSpot(r.getSpot());
        dto.setDuration(r.getDuration());
        dto.setBudget(r.getBudget());
        dto.setRoute(r.getRoute());
        dto.setAuthorName(r.getUser().getName());
        dto.setLiked(likeRepository.existsByReview_ReviewNoAndUser_UserId(reviewNo, currentUserId));
        return dto;
    }
}
