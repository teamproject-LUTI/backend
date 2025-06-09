package com.luti.luti.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExceptionCode {
    ;
    @Getter
    private int statusCode;

    @Getter
    private String statusDescription;
}