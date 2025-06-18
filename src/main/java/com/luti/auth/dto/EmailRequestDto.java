package com.luti.auth.dto;

import lombok.Getter;

@Getter
public class EmailRequestDto {
    private String email;

    public EmailRequestDto(String email) {
        this.email = email;
    }
}
