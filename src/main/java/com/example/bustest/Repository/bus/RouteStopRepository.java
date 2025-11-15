package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
    boolean existsByRoute_IdAndBusStop_Id(UUID routeId, UUID busStopId);
    java.util.List<RouteStop> findByRoute_IdOrderByStopOrder(UUID routeId);
    java.util.Optional<RouteStop> findByRoute_IdAndBusStop_Id(UUID routeId, UUID busStopId);
}

