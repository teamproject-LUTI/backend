package com.luti.board.controller;

import com.luti.board.dto.CommentRequestDto;
import com.luti.board.dto.CommentResponseDto;
import com.luti.board.entity.Comment.ParentType;
import com.luti.board.service.CommentService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     *
     * @param userId     작성자 사용자 ID
     * @param parentType 댓글 대상 타입(ASK or REVIEW)
     * @param parentId   댓글 대상 글 ID
     * @param dto        댓글 내용 DTO
     * @return 생성된 댓글 ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<Long> createComment(
            @RequestParam Long userId,
            @RequestParam ParentType parentType,
            @RequestParam Long parentId,
            @RequestBody @Valid CommentRequestDto dto
    ) {
        Long commentId = commentService.createComment(parentType, parentId, userId, dto);
        return new SingleResponseDto<>(commentId);
    }

    /**
     * 댓글 목록 조회
     *
     * @param parentType 댓글 대상 타입
     * @param parentId   댓글 대상 글 ID
     * @return 해당 글에 달린 댓글 리스트
     */
    @GetMapping
    public MultiResponseDto<CommentResponseDto> getComments(
            @RequestParam ParentType parentType,
            @RequestParam Long parentId
    ) {
        List<CommentResponseDto> comments = commentService.getComments(parentType, parentId);
        // 두 번째 인자로 Page 객체를 넘겨주면 페이징 가능, 여기서는 전체 리스트만 반환
        return new MultiResponseDto<>(comments, null);
    }

    /**
     * 댓글 수정
     *
     * @param commentId 댓글 ID
     * @param userId    요청 사용자 ID
     * @param dto       수정할 댓글 내용 DTO
     * @return 수정된 댓글 정보
     */
    @PatchMapping("/{commentId}")
    public SingleResponseDto<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestBody @Valid CommentRequestDto dto
    ) {
        CommentResponseDto updated = commentService.updateComment(commentId, userId, dto);
        return new SingleResponseDto<>(updated);
    }

    /**
     * 댓글 삭제 (soft delete 아님, 완전 삭제)
     *
     * @param commentId 댓글 ID
     * @param userId    요청 사용자 ID
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        commentService.deleteComment(commentId, userId);
    }
}
