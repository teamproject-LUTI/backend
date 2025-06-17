package com.luti.board.entity;

import com.luti.audit.Auditable;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "ask_attachment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class AskAttachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ask_attachment_Id", updatable = false, nullable = false)
    private Long askAttachmentId;

    /**
     * 소속된 문의글 (Ask 엔티티와 다대일 연관관계)
     * <p>
     * - fetch = LAZY: 실제 접근 시 로딩
     * - optional = false: 반드시 Ask에 속해야 함
     * - ON DELETE CASCADE: 문의글 삭제 시 첨부파일도 자동 삭제
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ask_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_ASK_ATTACHMENT_ASK",
                    foreignKeyDefinition = "FOREIGN KEY (ask_id) REFERENCES ask(ask_id) ON DELETE CASCADE")
    )
    private Ask ask;

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
    // 연관관계 편의 메서드
    // ========================================================

    /**
     * 이 첨부파일을 특정 Ask에 연결하고, Ask 측 리스트에도 추가
     *
     * @param ask 연결할 Ask 엔티티
     */
    public void linkToAsk(Ask ask) {
        this.ask = ask;
        if (!ask.getAttachments().contains(this)) {
            ask.getAttachments().add(this);
        }
    }

    /**
     * 현재 연결된 Ask와의 연관관계를 해제하고, Ask 측 리스트에서도 제거
     */
    public void unlinkFromAsk() {
        if (this.ask != null) {
            ask.getAttachments().remove(this);
            this.ask = null;
        }
    }
}
