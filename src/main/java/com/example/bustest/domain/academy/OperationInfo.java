package com.example.bustest.domain.academy;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 운영 정보
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationInfo {
    /**
     * 운영 시작 시간
     */
    LocalTime startTime = LocalTime.of(9, 0);

    /**
     * 운영 종료 시간
     */
    LocalTime endTime = LocalTime.of(18, 0);

    /**
     * 운영 요일
     */
    OperationType operationType = OperationType.MONDAY;

    /**
     * 운영 설명
     * <p>
     * 추석 연휴 (9/28 ~ 10/1) 동안은 운영하지 않습니다.
     * </p>
     */
    String description;

    /**
     * 운영 요일 타입
     */
    public enum OperationType {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }

    /**
     * 생성자 함수
     */
    public OperationInfo(LocalTime startTime, LocalTime endTime, OperationType operationType, String description) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.operationType = operationType;
        this.description = description;
    }
}