package com.luti.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "accomoinfo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccomoInfo {

    @Id
    @Column(name = "accomo_no")
    private Long accomoNo;

    @Column(name = "accomo_nm")
    private String accomoNm;

    @Column(name = "post_no")
    private Long postNo;

}
