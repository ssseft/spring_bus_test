package com.example.bustest.Controller.map;

import com.example.bustest.Repository.map.LocationEntryRepository;
import com.example.bustest.Service.map.NaviApiService;
import com.example.bustest.domain.map.LocationEntry;
import com.example.bustest.dto.map.CoordinateDTO;
import com.example.bustest.dto.map.RouteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final LocationEntryRepository repository;
    private final NaviApiService naviApiService;

    public RouteController(LocationEntryRepository repository, NaviApiService naviApiService) {
        this.repository = repository;
        this.naviApiService = naviApiService;
    }

    // 경로 생성 API
    // - 입력 예: { "orderedStopIds": [ 10, 12, 14 ] } (정류장 id를 선택 순서대로)
    // - 반환 예: { path: [...], distanceMeters: 1234, durationSeconds: 567 }
    @PostMapping("/directions")
    public ResponseEntity<?> directions(@RequestBody RouteRequest req) {
        List<Long> ids = Optional.ofNullable(req.getOrderedStopIds()).orElse(Collections.emptyList());
        if (ids.size() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least 2 stops required");
        }

        // id -> LocationEntry 매핑 조회 (입력 순서 보존을 위해 Map 후 순회)
        Map<Long, LocationEntry> byId = repository.findAllById(ids)
                .stream().collect(Collectors.toMap(LocationEntry::getId, it -> it));

        List<CoordinateDTO> orderedCoords = new ArrayList<>();
        for (Long id : ids) {
            LocationEntry le = byId.get(id);
            if (le == null) continue; // 잘못된 id 무시
            if (le.getLatitude() == null || le.getLongitude() == null) continue; // 좌표 없는 항목 무시
            orderedCoords.add(new CoordinateDTO(le.getLatitude(), le.getLongitude()));
        }

        if (orderedCoords.size() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Valid coordinates fewer than 2");
        }

        return naviApiService.directionsByOrderedCoords(orderedCoords)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Directions API failed"));
    }
}

