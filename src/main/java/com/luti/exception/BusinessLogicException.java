package com.luti.exception;

import lombok.Getter;

@Getter
public class BusinessLogicException extends RuntimeException {
    private ExceptionCode exceptionCode;

    public BusinessLogicException(ExceptionCode exceptionCode) {
        super(exceptionCode.getStatusDescription());
        this.exceptionCode = exceptionCode;
    }
}
