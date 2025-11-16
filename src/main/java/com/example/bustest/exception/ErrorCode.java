package com.example.bustest.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_LOGIN(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 로그인 ID 입니다."),

    // AcademyManager
    ACADEMY_MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMY_MANAGER-001", "학원 관리자를 찾을 수 없습니다."),

    // Academy
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMY-001", "학원을 찾을 수 없습니다."),

    // Driver
    DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "DRIVER-001", "기사를 찾을 수 없습니다."),

    // BusStop
    BUS_STOP_NOT_FOUND(HttpStatus.NOT_FOUND, "BUS_STOP-001", "정류장을 찾을 수 없습니다."),
    BUS_STOP_DESTINATION_NOT_FOUND(HttpStatus.NOT_FOUND, "BUS_STOP-002", "목적지를 찾을 수 없습니다."),

    // Route
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE-001", "노선을 찾을 수 없습니다."),

    // Schedule
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-001", "스케줄을 찾을 수 없습니다."),
    SCHEDULE_DAILY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_DAILY_PLAN-001", "일일 운행 계획을 찾을 수 없습니다."),
    SCHEDULE_DAILY_PLAN_CANCELED(HttpStatus.CONFLICT, "SCHEDULE_DAILY_PLAN-002", "해당 일일 계획은 취소 상태입니다."),

    // Schedule specific
    SCHEDULE_ASSIGNMENTS_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-002", "assignments 는 필수입니다."),
    SCHEDULE_STUDENT_DUPLICATED(HttpStatus.BAD_REQUEST, "SCHEDULE-003", "요청 내 학생이 중복되었습니다."),
    SCHEDULE_BUS_STOP_NOT_IN_ROUTE(HttpStatus.BAD_REQUEST, "SCHEDULE-004", "선택한 정류장은 해당 노선에 속하지 않습니다."),
    SCHEDULE_STUDENT_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "SCHEDULE-005", "해당 학생은 이미 배정되어 있습니다."),
    SCHEDULE_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-006", "배정을 찾을 수 없습니다."),
    SCHEDULE_ROUTE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-007", "routeId 는 필수입니다."),
    SCHEDULE_BUS_STOP_ID_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE-008", "busStopId 는 필수입니다."),

    // Student
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDENT-001", "학생을 찾을 수 없습니다."),
    SCHEDULE_STUDENT_ALREADY_RESERVED(HttpStatus.CONFLICT, "SCHEDULE_STUDENT-001", "이미 해당 스케줄에 예약되어 있습니다."),
    SCHEDULE_STUDENT_RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_STUDENT-002", "학생 예약을 찾을 수 없습니다."),

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-002", "잘못된 입력 값입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

