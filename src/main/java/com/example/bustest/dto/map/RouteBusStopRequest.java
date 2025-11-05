package com.example.bustest.dto.map;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

// BusStop(UUID) 기반 경로 요청 DTO
@Setter
@Getter
public class RouteBusStopRequest {
    private List<UUID> orderedBusStopIds;
}

