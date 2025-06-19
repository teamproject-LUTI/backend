package com.luti.board.entity;

import com.luti.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

/**
 * 후기(Review) 첨부파일 엔티티
 */
@Entity
<<<<<<< HEAD
@Table(name = "review_attachment")
=======
@Table(name = "review_Attachment")
>>>>>>> 7fd23416cbbb8a733f0f0caf9036447d7a57e4d3
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ReviewAttachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_attachment_id", updatable = false, nullable = false)
    private Long reviewAttachmentId;

    /**
     * 이 첨부파일이 속한 후기글
     * - fetch=LAZY: 실제 접근 시 로딩
     * - optional=false: 반드시 후기글과 연관
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "review_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_REVIEW_ATTACHMENT_REVIEW")
    )
    @ToString.Exclude  // 순환 참조 방지
    private Review review;

    /** 원본 파일명 */
    @Column(name = "file_nm", length = 200, nullable = false)
    private String fileName;

    /** 물리 저장 경로 */
    @Column(name = "pypath", length = 200, nullable = false)
    private String physicalPath;

    /** 논리 접근 경로(URL 등) */
    @Column(name = "lopath", length = 200, nullable = false)
    private String logicalPath;

    /** 파일 확장자(ex: jpg, png) */
    @Column(name = "extend", length = 5, nullable = false)
    private String extension;

    /** 파일 크기(Byte) */
    @Column(name = "size", nullable = false)
    private Long size;

    /**
     * Review와 양방향 연관관계 설정
     */
    public void linkToReview(Review review) {
        this.review = review;
        if (!review.getAttachments().contains(this)) {
            review.getAttachments().add(this);
        }
    }

    /**
     * Review와의 연관관계 해제
     */
    public void unlinkFromReview() {
        if (this.review != null) {
            review.getAttachments().remove(this);
            this.review = null;
        }
    }
}
