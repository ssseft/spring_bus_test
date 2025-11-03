package com.example.bustest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * BaseException처리 (모든 사용자 예외의 기본 처리)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleUserException(BaseException e) {
        log.error("User exception occurred", e);
        ErrorResponse response = new ErrorResponse(e.getErrorCode(), e.getMessage());
        return ResponseEntity
            .status(e.getHttpStatus())
            .body(response);
    }


    /**
     * Validation 예외 처리 (@Valid 실패 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error", e);

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse response = new ErrorResponse(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                errorMessage
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        ErrorResponse response = new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }
}
