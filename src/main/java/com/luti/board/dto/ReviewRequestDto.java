package com.luti.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String travelRegion;
    private String travelPeriod;
    private String spot;
    private String duration;
    private String budget;
    private String route;
}
