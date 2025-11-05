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

    // LocationEntry 기반 프리뷰는 제거되었습니다.

    // Preview (BusStop 기반 / UUID)
    @PostMapping({"/preview", "/directions-busstops"})
    public ResponseEntity<?> previewByBusStops(@RequestBody RouteBusStopRequest req) {
        List<UUID> ids = Optional.ofNullable(req.getOrderedBusStopIds()).orElse(Collections.emptyList());
        if (ids.size() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least 2 bus stops required");
        }

        Map<UUID, BusStop> byId = busStopRepository.findAllById(ids)
                .stream().collect(Collectors.toMap(BusStop::getId, it -> it));

        List<CoordinateDTO> orderedCoords = new ArrayList<>();
        for (UUID id : ids) {
            BusStop bs = byId.get(id);
            if (bs == null) continue;
            orderedCoords.add(new CoordinateDTO(bs.getLatitude().doubleValue(), bs.getLongitude().doubleValue()));
        }

        if (orderedCoords.size() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Valid bus stop coordinates fewer than 2");
        }

        return naviApiService.directionsByOrderedCoords(orderedCoords)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Directions API failed"));
    }

    @PostMapping("/academies/{academyId}")
    public ResponseEntity<CreateRouteResponse> create(@PathVariable UUID academyId,
                                                      @RequestBody CreateRouteRequest req) {
        CreateRouteResponse resp = routePersistService.create(academyId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/academies/{academyId}")
    public List<RouteListItem> listByAcademy(@PathVariable UUID academyId) {
        return routeQueryService.listByAcademy(academyId);
    }

    @GetMapping("/{routeId}")
    public RouteDetailResponse detail(@PathVariable UUID routeId) {
        return routeQueryService.getDetail(routeId);
    }
}
