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
@Table(
        name = "driving_students",
        uniqueConstraints = @UniqueConstraint(name = "uq_driving_student", columnNames = {"driving_id", "student_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DrivingStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driving_id", nullable = false)
    private Driving driving;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // 조회 성능을 위해 여기에도 중복 저장(굳이 필요 없으면 지워도 됨)
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_stop_id", nullable = false)
    private BusStop busStop;

    @Column(name = "planned_time", nullable = false)
    private LocalTime plannedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private drivingStudentStatus status; //default : reserved

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public DrivingStudent(Driving driving,
                          Student student,
                          LocalDate date,
                          BusStop busStop,
                          LocalTime plannedTime,
                          drivingStudentStatus status) {
        this.driving = driving;
        this.student = student;
        this.date = date;
        this.busStop = busStop;
        this.plannedTime = plannedTime;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(BusStop busStop,
                       LocalTime plannedTime,
                       drivingStudentStatus status) {
        if (busStop != null) this.busStop = busStop;
        if (plannedTime != null) this.plannedTime = plannedTime;
        if (status != null) this.status = status;
        this.updatedAt = Instant.now();
    }

    public enum drivingStudentStatus {
        reserved,
        boarded,
        completed,
        no_show,
        canceled
    }
}
