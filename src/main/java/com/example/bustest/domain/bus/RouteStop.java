package com.example.bustest.domain.bus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "route_stops",
    uniqueConstraints = @UniqueConstraint(name = "uq_route_stop", columnNames = {"route_id", "stop_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private BusStop busStop;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(name = "start_to_arrive_time", nullable = false)
    private LocalTime startToArriveTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder
    public RouteStop(Route route, BusStop busStop, Integer stopOrder, LocalTime startToArriveTime) {
        this.route = route;
        this.busStop = busStop;
        this.stopOrder = stopOrder;
        this.startToArriveTime = startToArriveTime;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(Integer stopOrder, LocalTime startToArriveTime) {
        if (stopOrder != null) this.stopOrder = stopOrder;
        if (startToArriveTime != null) this.startToArriveTime = startToArriveTime;
        this.updatedAt = Instant.now();
    }
}
