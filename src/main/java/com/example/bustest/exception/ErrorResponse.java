package com.example.bustest.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * 에러 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final String code;
    private final String message;
    private final Instant timestamp;

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.code = errorCode.getCode();
        this.message = message;
        this.timestamp = Instant.now();
    }

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
    }
}
