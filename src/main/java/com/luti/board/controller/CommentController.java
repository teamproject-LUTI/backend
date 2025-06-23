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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     * URL: POST /api/comments/{parentType}/{parentId}
     */
    @PostMapping("/{parentType}/{parentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<Long> createComment(
            @AuthenticationPrincipal Long userId,  // JWT에서 자동 추출
            @PathVariable ParentType parentType,   // URL 경로에서 추출
            @PathVariable Long parentId,           // URL 경로에서 추출
            @RequestBody @Valid CommentRequestDto dto
    ) {
        Long commentId = commentService.createComment(parentType, parentId, userId, dto);
        return new SingleResponseDto<>(commentId);
    }

    /**
     * 댓글 목록 조회
     * URL: GET /api/comments/{parentType}/{parentId}
     */
    @GetMapping("/{parentType}/{parentId}")
    public MultiResponseDto<CommentResponseDto> getComments(
            @PathVariable ParentType parentType,
            @PathVariable Long parentId,
            @AuthenticationPrincipal Long currentUserId  // JWT에서 현재 사용자 ID 추출 (null 가능)
    ) {
        // currentUserId가 null이어도 처리 가능하도록 수정
        List<CommentResponseDto> comments = commentService.getComments(parentType, parentId, currentUserId);
        return new MultiResponseDto<>(comments, null);
    }

    /**
     * 댓글 수정
     * URL: PATCH /api/comments/{commentId}
     */
    @PatchMapping("/{commentId}")
    public SingleResponseDto<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId,  // JWT에서 자동 추출
            @RequestBody @Valid CommentRequestDto dto
    ) {
        CommentResponseDto updated = commentService.updateComment(commentId, userId, dto);
        return new SingleResponseDto<>(updated);
    }

    /**
     * 댓글 삭제
     * URL: DELETE /api/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId  // JWT에서 자동 추출
    ) {
        commentService.deleteComment(commentId, userId);
    }
}