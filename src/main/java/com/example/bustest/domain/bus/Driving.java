package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

//운행 계획(일자별)
@Entity
@Table(name = "driving",
       uniqueConstraints = @UniqueConstraint(name = "uq_driving_schedule_date", columnNames = {"schedule_id", "date"}))
//스케쥴id : date 1:1 보장을 위해 uniqueconstraint추가
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Driving {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "date", nullable = false)
    private LocalDate date; //YYMMDD 예정

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private drivingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = true)
    private Route route;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public Driving(Schedule schedule,
                   LocalDate date,
                   drivingStatus status,
                   Route route) {
        this.schedule = schedule;
        this.date = date;
        this.status = status;
        this.route = route;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(drivingStatus status, Route route) {
        if (status != null) this.status = status;
        this.route = route;
        this.updatedAt = Instant.now();
    }

    public enum drivingStatus {
        scheduled,
        in_progress,
        completed,
        canceled
    }
}
