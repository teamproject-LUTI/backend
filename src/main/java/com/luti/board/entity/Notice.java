package com.luti.board.entity;

import com.luti.auth.entity.User;
import com.luti.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Notice extends Auditable {

    /** 공지글 고유번호 (PK, AUTO_INCREMENT) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id", updatable = false, nullable = false)
    private Long noticeId;

    /**
     * 게시글 작성자 정보 (User 엔티티와 다대일 연관관계)
     * <p>
     * - fetch = LAZY: 실제로 조회할 때만 User를 가져옴
     * - optional = false: 반드시 작성자가 있어야 함
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 게시글 제목 (최대 100자, NOT NULL) */
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    /** 본문 (HTML 등 긴 문자열 저장) */
    @Lob
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 조회수 (기본값 0) */
    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    public void increaseViewCount() {
        this.viewCount++;
    }

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 첨부파일 목록 (OneToMany 연관관계)
     * <p>
     * - cascade = ALL: Notice에 대한 변경이 Attachment로 전파됨
     * - orphanRemoval = true: Notice에서 분리된 Attachment는 자동 삭제됨
     * </p>
     */
    @OneToMany(
            mappedBy = "notice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<NoticeAttachment> attachments = new ArrayList<>();

    /**
     * 게시글을 삭제 상태로 표시
     * <p>
     * 실제 레코드는 남기되, 조회 시 제외하도록 soft delete 처리
     * </p>
     */
    public void markDeleted() {
        this.deleted = true;
    }

    /**
     * 첨부파일을 추가하고, 양쪽 연관관계를 설정
     *
     * @param attachment 추가할 NoticeAttachment
     */
    public void addAttachment(NoticeAttachment attachment) {
        attachments.add(attachment);
        attachment.setNotice(this);
    }

    /**
     * 첨부파일을 제거하고, 양쪽 연관관계를 제거
     *
     * @param attachment 제거할 NoticeAttachment
     */
    public void removeAttachment(NoticeAttachment attachment) {
        attachments.remove(attachment);
        attachment.setNotice(null);
    }


}
