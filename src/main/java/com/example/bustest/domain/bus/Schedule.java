package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/** 스케줄 템플릿 (schedules) */
@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // schedule는 불변인가? 아니면 변할 수 있는가를 고민해봤는데
    // schedule은 운행 default 템플릿으로 이해하고 일시적으로 학생 미탑승/타는 위치 변경은 ScheduleDailyPlan테이블에서 적용하기로 결정
    // run(운행)테이블 또한 ScheduleDailyPlan을 기반으로 생성
    // ScheduleDailyPlan은 한달 기준으로 생성 : ex) 선택한 Schedule 8월 일정만들기를 하면 8월 해당요일 전체 생성(여기서 학원 휴무일/버스 미운행 등 설정)
    // 예를 들어 한 학생이 일시적으로 승/하차하는 장소를 변경한다? -> 우선 Student/parent는 삭제는 가능하도록 설정(ScheduleDailyPlan에서 삭제는 가능)
    // 요청은 학생 -> 학원에게 요청 해서 ScheduleDailyPlan에서 노선 추가는 학원에서만 수정 가능하도록 한다.(이동시간 계산을 위해 노선을 재등록 해야함)
    // 특별 운행(일회성 운행)은 어떻게 할건가? 에 대해서는 Schedule에 추가 필드를 넣어야 할 거 같긴한데(repeat_days에 0을 일회성으로 한다던가,is_temp와 같은 필드) 이건 논의 필요
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 해당 필드는 사실 route_id에서 검증을 하기 때문에 필요 없을 것 같긴한데, 조회성능 향상을 위해 일단 넣음 삭제해도됨.
    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // 요일 반복 설정(비트마스크 등으로 사용)
    @Column(name = "repeat_days", nullable = false)
    private Integer repeatDays;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // 승하차 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "boarding_status", nullable = false, length = 20)
    private BoardingStatus boardingStatus;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public Schedule(UUID academyId,
                    Route route,
                    String name,
                    Integer repeatDays,
                    LocalTime startTime,
                    LocalTime endTime,
                    BoardingStatus boardingStatus,
                    Boolean isActive) {
        this.academyId = academyId;
        this.route = route;
        this.name = name;
        this.repeatDays = repeatDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.boardingStatus = boardingStatus;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(Route route,
                       String name,
                       Integer repeatDays,
                       LocalTime startTime,
                       LocalTime endTime,
                       BoardingStatus boardingStatus,
                       Boolean isActive) {
        if (route != null) this.route = route;
        if (name != null) this.name = name;
        if (repeatDays != null) this.repeatDays = repeatDays;
        if (startTime != null) this.startTime = startTime;
        if (endTime != null) this.endTime = endTime;
        if (boardingStatus != null) this.boardingStatus = boardingStatus;
        if (isActive != null) this.isActive = isActive;
        this.updatedAt = Instant.now();
    }

    /** 승/하차 구분 */
    public enum BoardingStatus {
        PICKUP,   // 승차
        DROPOFF   // 하차
    }
}

