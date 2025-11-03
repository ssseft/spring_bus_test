package com.example.bustest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 통합 예외 기본 클래스
 */
@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }
}