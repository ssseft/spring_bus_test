package com.example.bustest.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RouteDetailResponse {
    private final UUID id;
    private final String name;
    private final long totalTimeSeconds;
    private final long distanceMeters;
    private final long durationSeconds;
    private final List<CoordinateDTO> path;
}

