package com.luti.board.entity;

import com.luti.audit.Auditable;
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
public class NoticeAttachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_attachment_id", updatable = false, nullable = false)
    private Long noticeAttachmentId;

    /**
     * 소속된 공지글 정보 (Notice 엔티티와 다대일 연관관계)
     * - notice 테이블의 PK 컬럼은 notice_no 입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "notice_no",                 // ← DB 컬럼명에 맞춰 변경
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_NOTICE_ATTACHMENT_NOTICE")
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

    public void linkToNotice(Notice notice) {
        this.notice = notice;
        if (!notice.getAttachments().contains(this)) {
            notice.getAttachments().add(this);
        }
    }

    public void unlinkFromNotice() {
        if (notice != null) {
            notice.getAttachments().remove(this);
            this.notice = null;
        }
    }
}
