package com.example.bustest.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateRouteResponse {
    private final UUID routeId;
    private final RouteResponse summary;
}

