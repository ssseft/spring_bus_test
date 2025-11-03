package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** 정류장 */
@Entity
@Table(name = "bus_stops")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusStop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;   // decimal(10,8)

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;  // decimal(11,8)

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public BusStop(UUID academyId, String name,
                   BigDecimal latitude, BigDecimal longitude,
                   String photoUrl, Boolean isActive) {
        this.academyId = academyId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String name, BigDecimal latitude, BigDecimal longitude,
                       String photoUrl, Boolean isActive) {
        if (name != null) this.name = name;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (photoUrl != null) this.photoUrl = photoUrl;
        if (isActive != null) this.isActive = isActive;
        this.updatedAt = Instant.now();
    }
}