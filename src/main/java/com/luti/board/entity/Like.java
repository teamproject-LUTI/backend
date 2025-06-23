package com.luti.board.entity;

import com.luti.auth.entity.User;
import com.luti.board.entity.Review;
import com.luti.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "`like`") // MySQL 예약어 회피(백틱 포함)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Like extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id", updatable = false, nullable = false)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "review_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_LIKE_REVIEW")
    )
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_LIKE_USER")
    )
    private User user;

}
