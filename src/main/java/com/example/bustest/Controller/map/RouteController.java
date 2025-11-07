package com.example.bustest.Controller.map;

import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.Service.map.NaviApiService;
import com.example.bustest.Service.map.RoutePersistService;
import com.example.bustest.Service.map.RouteQueryService;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.dto.map.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    private final BusStopRepository busStopRepository;
    private final NaviApiService naviApiService;
    private final RoutePersistService routePersistService;
    private final RouteQueryService routeQueryService;

    // Preview (BusStop 기반 / UUID)
    @PostMapping({"/preview", "/directions-busstops"})
    public ResponseEntity<RoutePathResponse> previewByBusStops(@RequestBody RouteCreateRequest req) {
        List<UUID> ids = Optional.ofNullable(req.getOrderedBusStopIds()).orElse(Collections.emptyList());
        if (ids.size() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Map<UUID, BusStop> byId = busStopRepository.findAllById(ids)
                .stream().collect(Collectors.toMap(BusStop::getId, it -> it));

        // 존재하지 않는 정류장 ID가 있으면 명시적으로 오류 반환
        List<UUID> missing = ids.stream().filter(id -> !byId.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<CoordinateDTO> orderedCoords = ids.stream()
                .map(byId::get)
                .map(bs -> new CoordinateDTO(bs.getLatitude().doubleValue(), bs.getLongitude().doubleValue()))
                .toList();

        return naviApiService.directionsByOrderedCoords(orderedCoords)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());
    }

    @PostMapping("/academies/{academyId}")
    public ResponseEntity<RouteCreateResponse> create(@PathVariable UUID academyId,
                                                      @RequestBody RouteCreateRequest req) {
        RouteCreateResponse resp = routePersistService.create(academyId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/academies/{academyId}")
    public List<RouteSummaryResponse> listByAcademy(@PathVariable UUID academyId) {
        return routeQueryService.listByAcademy(academyId);
    }

    @GetMapping("/{routeId}")
    public RouteDetailResponse detail(@PathVariable UUID routeId) {
        return routeQueryService.getDetail(routeId);
    }
}
