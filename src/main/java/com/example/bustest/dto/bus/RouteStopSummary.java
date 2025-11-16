package com.example.bustest.dto.bus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RouteStopSummary {
    private final UUID busStopId;
    private final String busStopName;
    private final Integer order;
}

