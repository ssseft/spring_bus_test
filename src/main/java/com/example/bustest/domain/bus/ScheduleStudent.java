package com.example.bustest.domain.bus;

import com.example.bustest.domain.user.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "schedule_students",
    uniqueConstraints = @UniqueConstraint(name = "uq_schedule_student", columnNames = {"schedule_id", "student_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_stop_id", nullable = false)
    private BusStop busStop;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public ScheduleStudent(Schedule schedule, Student student, BusStop busStop) {
        this.schedule = schedule;
        this.student = student;
        this.busStop = busStop;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(BusStop busStop) {
        if (busStop != null) this.busStop = busStop;
        this.updatedAt = Instant.now();
    }
}
