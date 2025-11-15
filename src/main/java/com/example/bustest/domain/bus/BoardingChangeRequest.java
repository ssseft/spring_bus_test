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
@Table(name = "boarding_change_requests",
       indexes = {
               @Index(name = "idx_bcr_run_student_status", columnList = "run_id, student_id, status")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardingChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private Run run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stop_id", nullable = false)
    private BusStop fromStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stop_id", nullable = false)
    private BusStop toStop;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "processed_by")
    private UUID processedBy;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public enum Status { pending, approved, rejected }

    @Builder
    public BoardingChangeRequest(Run run,
                                 Student student,
                                 BusStop fromStop,
                                 BusStop toStop,
                                 Status status,
                                 String reason) {
        this.run = run;
        this.student = student;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.status = status;
        this.reason = reason;
        this.requestedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void approve(UUID processedBy) {
        this.status = Status.approved;
        this.processedBy = processedBy;
        this.processedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void reject(UUID processedBy, String rejectReason) {
        this.status = Status.rejected;
        this.processedBy = processedBy;
        this.rejectReason = rejectReason;
        this.processedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}

