package com.luti.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_attachment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_no")
    private Long fileNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_no", nullable = false)
    private Review review;

    @Column(name = "file_nm", length = 200)
    private String fileName;

    @Column(name = "pypath", length = 200)
    private String physicalPath;

    @Column(name = "lopath", length = 200)
    private String logicalPath;

    @Column(name = "extend", length = 5)
    private String extend;

    @Column(name = "size")
    private Integer size;
}