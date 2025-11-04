package com.example.bustest.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_LOGIN(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 로그인 ID입니다."),

    // AcademyManager 관련 에러
    ACADEMY_MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMY_MANAGER-001", "학원 관리자를 찾을 수 없습니다."),

    // Academy 관련 에러
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMY-001", "학원을 찾을 수 없습니다."),

    // Driver 관련 에러
    DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "DRIVER-001", "기사를 찾을 수 없습니다."),

    // BusStop 관련 에러
    BUS_STOP_NOT_FOUND(HttpStatus.NOT_FOUND, "BUS_STOP-001", "정류장을 찾을 수 없습니다."),
    BUS_STOP_DESTINATION_NOT_FOUND(HttpStatus.NOT_FOUND, "BUS_STOP-002", "위치를 찾을 수 없습니다."),

    // 일반 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-002", "잘못된 입력 값입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
