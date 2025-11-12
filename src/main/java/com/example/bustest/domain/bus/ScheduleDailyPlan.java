package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// 스케줄 일자별 계획
@Entity
@Table(name = "schedule_daily_plans",
       uniqueConstraints = @UniqueConstraint(name = "uq_schedule_date", columnNames = {"schedule_id", "date"}))
//스케쥴id : date 1:1 보장을 위해 uniqueconstraint추가
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleDailyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "date", nullable = false)
    private LocalDate date; //이건 YYMMDD로 파싱해서 받을 예정

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status Status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public ScheduleDailyPlan(Schedule schedule,
                             LocalDate date,
                             Status Status,
                             Route route) {
        this.schedule = schedule;
        this.date = date;
        this.Status = Status;
        this.route = route; // nullable
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(Status serviceStatus, Route route) {
        if (serviceStatus != null) this.Status = serviceStatus;
        this.route = route; // nullable allowed
        this.updatedAt = Instant.now();
    }

    public enum Status {
        OPER,      // 운행
        CANCELED   // 운행 취소/운행 안함
    }
}

