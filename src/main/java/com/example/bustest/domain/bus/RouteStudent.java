package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 노선-학생 매핑 */
@Entity
@Table(name = "routes_students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public RouteStudent(UUID routeId, UUID studentId, Boolean isActive, String memo) {
        this.routeId = routeId;
        this.studentId = studentId;
        this.isActive = isActive != null ? isActive : true;
        this.memo = memo;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(Boolean isActive, String memo) {
        if (isActive != null) this.isActive = isActive;
        if (memo != null) this.memo = memo;
        this.updatedAt = Instant.now();
    }
}
