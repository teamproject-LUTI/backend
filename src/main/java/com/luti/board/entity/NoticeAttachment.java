package com.luti.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "notice_attachment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class NoticeAttachment extends com.luti.audit.Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_no", updatable = false, nullable = false)
    private Long id;

    /**
     * 소속된 공지글 정보 (Notice 엔티티와 다대일 연관관계)
     * <p>
     * - fetch = LAZY: 실제로 접근할 때만 Notice를 로딩
     * - optional = false: 항상 Notice에 속해야 함
     * - ON DELETE CASCADE: 공지글 삭제 시 첨부파일도 함께 삭제
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "notice_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_NOTICE_ATTACHMENT_NOTICE",
                    foreignKeyDefinition = "FOREIGN KEY (notice_id) REFERENCES notice(notice_id) ON DELETE CASCADE")
    )
    private Notice notice;

    /** 원본 파일명 */
    @Column(name = "file_nm", length = 200, nullable = false)
    private String fileName;

    /** 실제 저장된 물리 경로 */
    @Column(name = "pypath", length = 200, nullable = false)
    private String physicalPath;

    /** 논리 경로(URI) */
    @Column(name = "lopath", length = 200, nullable = false)
    private String logicalPath;

    /** 확장자(ex: jpg, png) */
    @Column(name = "extend", length = 10, nullable = false)
    private String extension;

    /** 파일 크기(bytes) */
    @Column(name = "size", nullable = false)
    private Long size;

    // ========================================================
    // 편의 메서드
    // ========================================================

    /**
     * 이 첨부파일을 특정 Notice에 연결하고, 연관관계를 양쪽으로 설정
     *
     * @param notice 연결할 Notice 엔티티
     */
    public void linkToNotice(Notice notice) {
        this.notice = notice;
        if (!notice.getAttachments().contains(this)) {
            notice.getAttachments().add(this);
        }
    }

    /**
     * 이 첨부파일을 현재 Notice와의 연관관계를 해제하고
     * Notice 측 리스트에서도 제거
     */
    public void unlinkFromNotice() {
        if (this.notice != null) {
            List<NoticeAttachment> attachments = this.notice.getAttachments();
            attachments.remove(this);
            this.notice = null;
        }
    }
}
