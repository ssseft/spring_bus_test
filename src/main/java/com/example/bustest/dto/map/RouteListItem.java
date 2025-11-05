package com.example.bustest.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RouteListItem {
    private final UUID id;
    private final String name;
    private final long totalTimeSeconds;
    private final Instant createdAt;
}

