package com.luti.board.dto;

import com.luti.board.entity.Ask;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AskResponseDto {
    /** 문의글 고유번호 */
    private Long askId;

    /** 작성자 사용자 ID */
    private Long userId;

    /** 사용자 이름 */
    private String userName;

    /** 문의글 제목 */
    private String title;

    /** 문의글 내용 */
    private String content;

    /** 답변 여부 */
    private Boolean answered;

    /** 생성 시각 */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime modifiedAt;

    /** 본인 작성 여부 */
    private Boolean owner;

    /** 관리자 여부 */
    private Boolean isAdmin;

    /**
     * Entity → DTO 변환 (기본 메서드)
     *
     * @param ask              변환할 Ask 엔티티
     * @param userId    현재 로그인한 사용자 ID (SecurityContext 에서 꺼내온 값)
     * @return AskResponseDto 인스턴스
     */
    public static AskResponseDto of(Ask ask, Long userId) {
        boolean isOwner = ask.getUser().getUserId().equals(userId);
        return new AskResponseDto(
                ask.getAskId(),
                ask.getUser().getUserId(),
                ask.getUser().getName(),
                ask.getTitle(),
                ask.getContent(),
                ask.getAnswered(),
                ask.getCreatedAt(),
                ask.getModifiedAt(),
                isOwner,
                false  // 기본값, Service에서 별도 설정
        );
    }

    /**
     * Entity → DTO 변환 (관리자 여부 포함)
     *
     * @param ask              변환할 Ask 엔티티
     * @param userId    현재 로그인한 사용자 ID
     * @param isAdmin   관리자 여부
     * @return AskResponseDto 인스턴스
     */
    public static AskResponseDto of(Ask ask, Long userId, boolean isAdmin) {
        boolean isOwner = ask.getUser().getUserId().equals(userId);
        return new AskResponseDto(
                ask.getAskId(),
                ask.getUser().getUserId(),
                ask.getUser().getName(),
                ask.getTitle(),
                ask.getContent(),
                ask.getAnswered(),
                ask.getCreatedAt(),
                ask.getModifiedAt(),
                isOwner,
                isAdmin
        );
    }
}