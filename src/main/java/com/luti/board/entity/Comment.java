package com.luti.board.entity;

import com.luti.auth.entity.User;
import com.luti.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

/**
 * 단일 댓글 테이블을 위한 엔티티 (Polymorphic)
 *
 * parentType: 댓글 대상 타입 (ASK 또는 REVIEW)
 * parentId  : 댓글 대상 엔티티의 PK
 */
@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Comment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_no", updatable = false, nullable = false)
    private Long commentNo;

    /** 댓글 대상 타입: ASK 또는 REVIEW */
    @Column(name = "parent_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ParentType parentType;

    /** 댓글 대상 ID (ask_no 또는 review_no) */
    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    /** 댓글 작성자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_COMMENT_USER"))
    private User user;

    /** 댓글 본문 */
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * 댓글 본문을 갱신합니다.
     */
    public void updateContent(String newContent) {
        this.content = newContent;
    }

    /** 댓글 대상 타입 열거형 */
    public enum ParentType {
        ASK, REVIEW
    }
}
