package com.example.bustest.dto.map;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateRouteRequest {
    private String name;
    private List<UUID> orderedBusStopIds;
}

