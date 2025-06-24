package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.LikeResponseDto;
import com.luti.board.dto.LikedReviewDto;
import com.luti.board.dto.LikedReviewSearchDto;
import com.luti.board.entity.Like;
import com.luti.board.entity.Review;
import com.luti.board.repository.LikeRepository;
import com.luti.board.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    /** 내가 좋아요 누른 게시글 목록 (기존 방식 - 하위 호환) */
    @Transactional(readOnly = true)
    public List<LikedReviewDto> getLikedReviews(Long userId) {
        return likeRepository.findLikedReviewsByUserId(userId).stream()
                .map(this::convertToLikedReviewDto)
                .collect(Collectors.toList());
    }

    /**
     * 페이지네이션과 검색 조건을 적용한 좋아요 누른 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<LikedReviewDto> getLikedReviewsWithPagination(Long userId, LikedReviewSearchDto searchDto) {
        log.info("좋아요 누른 리뷰 조회 - 사용자 ID: {}, 검색 조건: {}", userId, searchDto);

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());

        // 검색 조건에 따라 적절한 Repository 메서드 호출
        Page<Like> likePage = findLikesByCondition(userId, searchDto, pageable);

        // Like 엔티티를 LikedReviewDto로 변환
        return likePage.map(like -> convertToLikedReviewDto(like.getReview()));
    }

    /**
     * 검색 조건에 따라 적절한 Repository 메서드를 호출하는 헬퍼 메서드
     */
    private Page<Like> findLikesByCondition(Long userId, LikedReviewSearchDto searchDto, Pageable pageable) {
        String searchType = searchDto.getSearchType();
        String keyword = searchDto.getKeyword();
        String sortBy = searchDto.getSortBy();

        // 검색어가 없는 경우
        if (!StringUtils.hasText(keyword)) {
            if ("oldest".equals(sortBy)) {
                return likeRepository.findLikedReviewsByUserIdOrderByCreatedAtAsc(userId, pageable);
            } else {
                return likeRepository.findLikedReviewsByUserIdOrderByCreatedAtDesc(userId, pageable);
            }
        }

        // 검색어가 있는 경우
        if (!StringUtils.hasText(searchType) || "all".equals(searchType.toLowerCase())) {
            // 통합 검색 (searchType이 없거나 "all"인 경우)
            if ("oldest".equals(sortBy)) {
                return likeRepository.findLikedReviewsByUserIdAndAllFieldsContainingOrderByCreatedAtAsc(userId, keyword, pageable);
            } else {
                return likeRepository.findLikedReviewsByUserIdAndAllFieldsContainingOrderByCreatedAtDesc(userId, keyword, pageable);
            }
        }

        // 기존 개별 필드 검색 로직
        switch (searchType.toLowerCase()) {
            case "title":
                if ("oldest".equals(sortBy)) {
                    return likeRepository.findLikedReviewsByUserIdAndTitleContainingOrderByCreatedAtAsc(userId, keyword, pageable);
                } else {
                    return likeRepository.findLikedReviewsByUserIdAndTitleContainingOrderByCreatedAtDesc(userId, keyword, pageable);
                }
            case "content":
                if ("oldest".equals(sortBy)) {
                    return likeRepository.findLikedReviewsByUserIdAndContentContainingOrderByCreatedAtAsc(userId, keyword, pageable);
                } else {
                    return likeRepository.findLikedReviewsByUserIdAndContentContainingOrderByCreatedAtDesc(userId, keyword, pageable);
                }
            case "author":
                if ("oldest".equals(sortBy)) {
                    return likeRepository.findLikedReviewsByUserIdAndAuthorContainingOrderByCreatedAtAsc(userId, keyword, pageable);
                } else {
                    return likeRepository.findLikedReviewsByUserIdAndAuthorContainingOrderByCreatedAtDesc(userId, keyword, pageable);
                }
            default:
                //기본값도 통합 검색으로 변경
                if ("oldest".equals(sortBy)) {
                    return likeRepository.findLikedReviewsByUserIdAndAllFieldsContainingOrderByCreatedAtAsc(userId, keyword, pageable);
                } else {
                    return likeRepository.findLikedReviewsByUserIdAndAllFieldsContainingOrderByCreatedAtDesc(userId, keyword, pageable);
                }
        }
    }
    /**
     * Review 엔티티를 LikedReviewDto로 변환하는 헬퍼 메서드
     */
    private LikedReviewDto convertToLikedReviewDto(Review review) {
        return LikedReviewDto.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .authorName(review.getUser().getDisplayName())
                .likeCount(review.getLikeCount())
                .liked(true)  // 이미 좋아요 누른 글들이니까 항상 true
                .thumbnailPath(extractFirstImageUrl(review.getContent()))
                .content(review.getContent())
                .viewCount(review.getViewCount())
                .travelRegion(review.getTravelRegion())
                .travelPeriod(review.getTravelPeriod())
                .build();
    }

    /** 상세·목록 페이지에서 클릭 전 좋아요 여부 확인 */
    @Transactional(readOnly = true)
    public boolean isLiked(Long reviewId, Long userId) {
        return likeRepository.existsByReviewReviewIdAndUserUserId(reviewId, userId);
    }

    /** 특정 사용자가 받은 좋아요 총 개수 */
    public long getTotalReceivedLikes(Long userId) {
        return likeRepository.countByReviewUserUserId(userId);
    }

    private String extractFirstImageUrl(String html) {
        if (html == null) return null;
        Pattern p = Pattern.compile(
                "<img[^>]*src=[\"']?([^\"'>]+)[\"']?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
