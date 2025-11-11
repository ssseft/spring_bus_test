package com.example.bustest.domain.bus;

import com.example.bustest.domain.user.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_daily_plan_id", nullable = false)
    private ScheduleDailyPlan scheduleDailyPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // 조회 성능을 위해 여기에도 중복 저장(굳이 필요 없으면 지워도 될거같긴 함)
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_stop_id", nullable = false)
    private BusStop busStop;

    @Column(name = "planned_time", nullable = false)
    private LocalTime plannedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", nullable = false)
    private ScheduleStatus scheduleStatus; //cancel,reserve로 구분

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public ScheduleStudent(ScheduleDailyPlan scheduleDailyPlan,
                           Student student,
                           LocalDate date,
                           BusStop busStop,
                           LocalTime plannedTime,
                           ScheduleStatus scheduleStatus) {
        this.scheduleDailyPlan = scheduleDailyPlan;
        this.student = student;
        this.date = date;
        this.busStop = busStop;
        this.plannedTime = plannedTime;
        this.scheduleStatus = scheduleStatus;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(BusStop busStop,
                       LocalTime plannedTime,
                       ScheduleStatus scheduleStatus) {
        if (busStop != null) this.busStop = busStop;
        if (plannedTime != null) this.plannedTime = plannedTime;
        if (scheduleStatus != null) this.scheduleStatus = scheduleStatus;
        this.updatedAt = Instant.now();
    }

    public enum ScheduleStatus {
        RESERVED,
        CANCELED
    }
}

