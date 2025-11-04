package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
 

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

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "geom", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point geom; // 위경도를 point로 저장


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
                   Point geom,
                   String photoUrl, Boolean isActive) {
        this.academyId = academyId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (geom == null) throw new BaseException(ErrorCode.BUS_STOP_DESTINATION_NOT_FOUND);
        if (geom.getSRID() != 4326) geom.setSRID(4326);
        this.geom = geom;
    }


    // 나중에 위도,경도 호출할 때 사용
    public BigDecimal getLatitude() {
        return BigDecimal.valueOf(geom.getY());
    }

    public BigDecimal getLongitude() {
        return BigDecimal.valueOf(geom.getX());
    }

    // 서비스에서 사용: Point로 geom 설정
    public void setGeom(Point geom) {
        //geom 은 notnull로 받을거긴한데 혹시 모르니 체크
        if (geom == null) throw new BaseException(ErrorCode.BUS_STOP_DESTINATION_NOT_FOUND);
        // SRID 4326은 표준 위도/경도
        if (geom.getSRID() != 4326) geom.setSRID(4326);
        this.geom = geom;
        this.updatedAt = Instant.now();
    }

    public void update(String name, Point geom,
                       String photoUrl, Boolean isActive) {
        if (name != null) this.name = name;
        if (geom != null) setGeom(geom);
        if (photoUrl != null) this.photoUrl = photoUrl;
        if (isActive != null) this.isActive = isActive;
        this.updatedAt = Instant.now();
    }
}
