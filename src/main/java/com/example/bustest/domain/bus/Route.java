package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 노선 (routes) */
@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nav_response", nullable = false, columnDefinition = "jsonb")
    private String navResponse;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "total_time", nullable = false)
    private LocalTime totalTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public Route(UUID academyId, String navResponse, String name, LocalTime totalTime) {
        this.academyId = academyId;
        this.navResponse = navResponse;
        this.name = name;
        this.totalTime = totalTime;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String navResponse, String name, LocalTime totalTime) {
        if (navResponse != null) this.navResponse = navResponse;
        if (name != null) this.name = name;
        if (totalTime != null) this.totalTime = totalTime;
        this.updatedAt = Instant.now();
    }
}
