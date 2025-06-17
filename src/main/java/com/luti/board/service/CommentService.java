package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.CommentRequestDto;
import com.luti.board.dto.CommentResponseDto;
import com.luti.board.entity.Comment;
import com.luti.board.entity.Comment.ParentType;
import com.luti.board.repository.CommentRepository;
import com.luti.board.repository.AskRepository;
import com.luti.board.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 기능을 담당하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AskRepository askRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 주어진 대상(ASK 혹은 REVIEW)에 댓글을 등록합니다.
     *
     * @param parentType 댓글 대상 타입 (ASK 또는 REVIEW)
     * @param parentId   댓글 대상 ID
     * @param userId     댓글 작성자 User ID
     * @param request    댓글 등록 요청 DTO
     * @return 생성된 댓글 ID
     */
    @Transactional
    public Long createComment(ParentType parentType,
                              Long parentId,
                              Long userId,
                              CommentRequestDto request) {
        // 1. 작성자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID=" + userId));

        // 2. 댓글 대상(ASK/REVIEW) 존재 확인
        switch (parentType) {
            case ASK:
                askRepository.findById(parentId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. ID=" + parentId));
                break;
            case REVIEW:
                reviewRepository.findById(parentId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 후기글입니다. ID=" + parentId));
                break;
            default:
                throw new IllegalArgumentException("올바르지 않은 parentType입니다: " + parentType);
        }

        // 3. Comment 엔티티 생성 및 저장
        Comment comment = Comment.builder()
                .parentType(parentType)
                .parentId(parentId)
                .user(user)
                .content(request.getContent())
                .build();

        commentRepository.save(comment);
        return comment.getCommentId();
    }

    /**
     * 특정 대상(ASK/REVIEW)에 달린 댓글 목록을 조회합니다.
     *
     * @param parentType 댓글 대상 타입
     * @param parentId   댓글 대상 ID
     * @return 댓글 응답 DTO 리스트
     */
    public List<CommentResponseDto> getComments(ParentType parentType, Long parentId) {
        return commentRepository
                .findAllByParentTypeAndParentId(parentType, parentId)
                .stream()
                .map(CommentResponseDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 댓글을 수정합니다. 본인 작성 댓글만 수정할 수 있습니다.
     *
     * @param commentId  수정할 댓글 ID
     * @param userId     요청 사용자 ID
     * @param request    댓글 수정 요청 DTO
     * @return 수정된 댓글의 응답 DTO
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, Long userId, CommentRequestDto request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. ID=" + commentId));

        // 본인 검증
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 업데이트
        comment.updateContent(request.getContent());
        return CommentResponseDto.of(comment);
    }

    /**
     * 댓글을 삭제합니다. 본인 작성 댓글만 삭제할 수 있습니다.
     *
     * @param commentId 댓글 ID
     * @param userId    요청 사용자 ID
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. ID=" + commentId));

        // 본인 검증
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}
