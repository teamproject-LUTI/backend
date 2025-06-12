package com.luti.board.entity;

import com.luti.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import com.luti.audit.Auditable;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE review SET is_deleted = true WHERE review_no = ?")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Review extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_no")
    private Long reviewNo;

    /** 작성자 정보 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    /** 후기 게시글 제목 */
    @Column(name = "title", length = 50, nullable = false)
    private String title;

    /** 후기 게시글 내용 */
    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    /** 조회수 */
    @Column(name = "view_count")
    private int viewCount = 0;

    /** 좋아요  */
    @Column(name = "like_count")
    private int likeCount = 0;


    /** 삭제 여부 플래그 - soft delete */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** 여행 지역 */
    @Column(name = "travel_region", length = 255)
    private String travelRegion;

    /** 여행 기간 */
    @Column(name = "travel_period", length = 255)
    private String travelPeriod;

}
