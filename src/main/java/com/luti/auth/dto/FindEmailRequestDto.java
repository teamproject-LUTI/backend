package com.luti.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindEmailRequestDto {
    private String name;
    private String phoneNumber;
}