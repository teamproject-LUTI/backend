package com.luti.auth.dto;

import com.luti.auth.enums.EmailCheckType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationRequestDto {
    private String email;
    private String name;
    private EmailCheckType checkType;
}
