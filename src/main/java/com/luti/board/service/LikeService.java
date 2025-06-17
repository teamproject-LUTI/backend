package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.LikeResponseDto;
import com.luti.board.dto.LikedReviewDto;
import com.luti.board.entity.Like;
import com.luti.board.entity.Review;
import com.luti.board.repository.LikeRepository;
import com.luti.board.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /** 좋아요 누르기 */
    @Transactional
    public LikeResponseDto likeReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. reviewId=" + reviewId));

        boolean alreadyLiked = likeRepository.existsByReviewReviewIdAndUserUserId(reviewId, userId);
        if (!alreadyLiked) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다. id=" + userId));

            Like like = Like.builder()
                    .review(review)
                    .user(user)
                    .build();
            likeRepository.save(like);

            // likeCount를 Review에서 직접 관리하는 경우 업데이트
            review.setLikeCount(review.getLikeCount() + 1);
        }

        int count = (int) likeRepository.countByReviewReviewId(reviewId);
        return LikeResponseDto.builder()
                .reviewId(reviewId)
                .likeCount(count)
                .liked(true)
                .build();
    }

    /** 좋아요 취소 */
    @Transactional
    public LikeResponseDto unlikeReview(Long reviewId, Long userId) {
        boolean exists  = likeRepository.existsByReviewReviewIdAndUserUserId(reviewId, userId);
        if (exists) {
            // 삭제 쿼리로 실제 좋아요 취소 처리
            likeRepository.deleteByReviewReviewIdAndUserUserId(reviewId, userId);

            // Review 엔티티의 likeCount도 함께 감소
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. reviewId=" + reviewId));
            review.setLikeCount(review.getLikeCount() - 1);
        }
        int count = (int) likeRepository.countByReviewReviewId(reviewId);
        return LikeResponseDto.builder()
                .reviewId(reviewId)
                .likeCount(count)
                .liked(false)
                .build();
    }

    /** 내가 좋아요 누른 게시글 목록 */
    @Transactional(readOnly = true)
    public List<LikedReviewDto> getLikedReviews(Long userId) {
        return likeRepository.findAllByUserUserId(userId).stream()
                .map(like -> {
                    Review r = like.getReview();
                    return LikedReviewDto.builder()
                            .reviewId(r.getReviewId())
                            .title(r.getTitle())
                            .authorName(r.getUser().getDisplayName()) // 작성자 필드명 author 기준
                            .likeCount(r.getLikeCount())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 상세·목록 페이지에서 클릭 전 좋아요 여부 확인 */
    @Transactional(readOnly = true)
    public boolean isLiked(Long reviewId, Long userId) {
        return likeRepository.existsByReviewReviewIdAndUserUserId(reviewId, userId);
    }
}


