package com.example.bustest.Service.map;

import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.dto.map.CoordinateDTO;
import com.example.bustest.dto.map.RouteCreateRequest;
import com.example.bustest.dto.map.RouteCreateResponse;
import com.example.bustest.Service.map.NaviApiService.NaviResult;
import com.example.bustest.dto.map.RoutePathResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoutePersistService {
    private final BusStopRepository busStopRepository;
    private final NaviApiService naviApiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PersistenceContext
    private EntityManager em;

    public RoutePersistService(BusStopRepository busStopRepository, NaviApiService naviApiService) {
        this.busStopRepository = busStopRepository;
        this.naviApiService = naviApiService;
    }

    @Transactional
    public RouteCreateResponse create(UUID academyId, RouteCreateRequest req) {
        List<UUID> ids = Optional.ofNullable(req.getOrderedBusStopIds()).orElse(Collections.emptyList());
        if (ids.size() < 2) throw new IllegalArgumentException("At least 2 bus stops required");

        Map<UUID, BusStop> byId = busStopRepository.findAllById(ids)
                .stream().collect(Collectors.toMap(BusStop::getId, it -> it));

        // 순서 보존 및 소속 학원 검증
        List<BusStop> orderedStops = new ArrayList<>();
        for (UUID id : ids) {
            BusStop bs = byId.get(id);
            if (bs == null) throw new IllegalArgumentException("Invalid bus stop id: " + id);
            if (!academyId.equals(bs.getAcademyId()))
                throw new IllegalArgumentException("Bus stop not in academy: " + id);
            orderedStops.add(bs);
        }

        List<CoordinateDTO> coords = orderedStops.stream()
                .map(bs -> new CoordinateDTO(bs.getLatitude().doubleValue(), bs.getLongitude().doubleValue()))
                .toList();

        NaviResult navi = naviApiService.directionsWithRaw(coords)
                .orElseThrow(() -> new IllegalStateException("Directions API failed"));

        RoutePathResponse summary = navi.getSummary();
        long duration = summary.getDurationSeconds();
        LocalTime total = LocalTime.ofSecondOfDay(Math.floorMod(duration, 24 * 3600));

        UUID routeId = UUID.randomUUID();
        String name = (req.getName() == null || req.getName().isBlank())
                ? ("Route-" + LocalDateTime.now())
                : req.getName();

        try {
            String rawJson = objectMapper.writeValueAsString(navi.getRaw());
            // routes insert
            em.createNativeQuery("INSERT INTO routes (id, academy_id, nav_response, name, total_time, created_at, updated_at) " +
                            "VALUES (:id, :academyId, CAST(:nav AS jsonb), :name, :totalTime, NOW(), NOW())")
                    .setParameter("id", routeId)
                    .setParameter("academyId", academyId)
                    .setParameter("nav", rawJson)
                    .setParameter("name", name)
                    .setParameter("totalTime", Time.valueOf(total))
                    .executeUpdate();

            // route_stops insert (순서 1부터)
            for (int i = 0; i < orderedStops.size(); i++) {
                BusStop bs = orderedStops.get(i);
                em.createNativeQuery("INSERT INTO route_stops (id, route_id, stop_id, stop_order, start_to_arrive_time, created_at, updated_at) " +
                                "VALUES (:id, :routeId, :stopId, :ord, :sta, NOW(), NOW())")
                        .setParameter("id", UUID.randomUUID())
                        .setParameter("routeId", routeId)
                        .setParameter("stopId", bs.getId())
                        .setParameter("ord", i + 1)
                        .setParameter("sta", Time.valueOf(LocalTime.MIDNIGHT))
                        .executeUpdate();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Route persist failed", e);
        }

        return new RouteCreateResponse(routeId, summary);
    }
}
