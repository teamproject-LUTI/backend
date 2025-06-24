package com.luti.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequestDto {
    private String name;
    private String email;
}
