package com.example.bustest.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RoutePreviewResponse {
    private final List<CoordinateDTO> path;
    private final long distanceMeters;
    private final long durationSeconds;
}

