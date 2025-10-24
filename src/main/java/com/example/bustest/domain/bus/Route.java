package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/** 노선 */
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

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 16)
    private RouteDirection direction;

    /** 1..127 (MON=1, …, SUN=64) */
    @Column(name = "weekdays_mask", nullable = false)
    private Integer weekdaysMask;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

    @Column(name = "driver_id")
    private UUID driverId;

    /** 학원 좌표 (참조 복사값; FK 아님) */
    @Column(name = "academy_latitude", precision = 10, scale = 8)
    private BigDecimal academyLatitude;

    @Column(name = "academy_longitude", precision = 11, scale = 8)
    private BigDecimal academyLongitude;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public enum RouteDirection {
        PICKUP,  // 등원
        DROPOFF  // 하교
    }

    @Builder
    public Route(UUID academyId, String name, RouteDirection direction,
                 Integer weekdaysMask, LocalTime departureTime,
                 Integer etaMinutes, UUID vehicleId, UUID driverId,
                 BigDecimal academyLatitude, BigDecimal academyLongitude,
                 Boolean isActive) {
        this.academyId = academyId;
        this.name = name;
        this.direction = direction;
        this.weekdaysMask = weekdaysMask;
        this.departureTime = departureTime;
        this.etaMinutes = etaMinutes;
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.academyLatitude = academyLatitude;
        this.academyLongitude = academyLongitude;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String name, RouteDirection direction, Integer weekdaysMask, LocalTime departureTime,
                       Integer etaMinutes, UUID vehicleId, UUID driverId,
                       BigDecimal academyLatitude, BigDecimal academyLongitude, Boolean isActive) {
        if (name != null) this.name = name;
        if (direction != null) this.direction = direction;
        if (weekdaysMask != null) this.weekdaysMask = weekdaysMask;
        if (departureTime != null) this.departureTime = departureTime;
        if (etaMinutes != null) this.etaMinutes = etaMinutes;
        if (vehicleId != null) this.vehicleId = vehicleId;
        if (driverId != null) this.driverId = driverId;
        if (academyLatitude != null) this.academyLatitude = academyLatitude;
        if (academyLongitude != null) this.academyLongitude = academyLongitude;
        if (isActive != null) this.isActive = isActive;
        this.updatedAt = Instant.now();
    }
}
