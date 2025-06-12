
package com.luti.board.entity;

import com.luti.auth.entity.User;
import com.luti.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ask")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Ask extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ask_no", updatable = false, nullable = false)
    private Long id;

    /**
     * 작성자 정보 (User 엔티티와 N:1 연관관계)
     * <p>
     * - fetch = LAZY: 실제 접근 시 로딩
     * - optional = false: 반드시 작성자가 있어야 함
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_ASK_USER"))
    private User user;

    /** 문의글 제목 (최대 100자, NOT NULL) */
    @Column(name = "qna_title", length = 100, nullable = false)
    private String title;

    /** 문의글 내용 (Lob: 대용량 텍스트) */
    @Lob
    @Column(name = "qna_content", nullable = false)
    private String content;

    /**
     * 답변 여부 플래그
     * <p>
     * - false: 미답변
     * - true: 답변 완료
     * </p>
     */
    @Column(name = "reply_yn", nullable = false)
    private Boolean answered = false;

    /**
     * 첨부파일 목록 (OneToMany 연관관계)
     * <p>
     * - cascade = ALL: Ask 변경 시 Attachment로 전파
     * - orphanRemoval = true: Ask에서 분리된 Attachment는 자동 삭제
     * </p>
     */
    @OneToMany(
            mappedBy = "ask",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AskAttachment> attachments = new ArrayList<>();

    /**
     * 댓글 목록 (OneToMany 연관관계)
     * <p>
     * - cascade = ALL: Ask 변경 시 Comment로 전파
     * - orphanRemoval = true: Ask에서 분리된 Comment는 자동 삭제
     * </p>
     */
    @OneToMany(
            mappedBy = "ask",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Comment> comments = new ArrayList<>();

    // ========================================================
    // 편의 메서드 (비즈니스 로직 캡슐화)
    // ========================================================

    /**
     * 답변 상태를 “답변 완료”로 변경
     */
    public void markAnswered() {
        this.answered = true;
    }

    /**
     * 첨부파일을 추가하고, 양방향 연관관계를 설정
     *
     * @param attachment NoticeAttachment 대신 AskAttachment 엔티티
     */
    public void addAttachment(AskAttachment attachment) {
        attachments.add(attachment);
        attachment.setAsk(this);
    }

    /**
     * 첨부파일을 제거하고, 양방향 연관관계를 해제
     *
     * @param attachment 제거할 AskAttachment
     */
    public void removeAttachment(AskAttachment attachment) {
        attachments.remove(attachment);
        attachment.setAsk(null);
    }

}
