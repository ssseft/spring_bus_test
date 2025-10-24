package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 정류장-학생 매핑 */
@Entity
@Table(name = "bus_stops_students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusStopStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** 정류장 ID (FK는 DDL에서 설정 권장: ON DELETE CASCADE) */
    @Column(name = "stop_id", nullable = false)
    private UUID stopId;

    /** 학생 ID (users.id 또는 students.id) */
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public BusStopStudent(UUID stopId, UUID studentId, String memo, Boolean isActive) {
        this.stopId = stopId;
        this.studentId = studentId;
        this.memo = memo;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String memo, Boolean isActive) {
        if (memo != null) this.memo = memo;
        if (isActive != null) this.isActive = isActive;
        this.updatedAt = Instant.now();
    }
}
