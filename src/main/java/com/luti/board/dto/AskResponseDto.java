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

    /**
     * Entity → DTO 변환 메서드
     *
     * @param ask 변환할 Ask 엔티티
     * @return AskResponseDto 인스턴스
     */
    public static AskResponseDto of(Ask ask) {
        return new AskResponseDto(
                ask.getAskId(),                          // Ask 엔티티 PK
                ask.getUser().getUserId(),          // User 엔티티의 userId
                ask.getUser().getName(),
                ask.getTitle(),                       // 문의 제목
                ask.getContent(),                     // 문의 내용
                ask.getAnswered(),                    // 답변 여부
                ask.getCreatedAt(),                   // 생성 시각 (Auditable 상속)
                ask.getModifiedAt()                   // 수정 시각 (Auditable 상속)
        );
    }
}
