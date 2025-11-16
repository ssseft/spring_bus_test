package com.example.bustest.dto.schedule;

import lombok.Data;

import java.util.UUID;

@Data
public class ScheduleRouteChangeRequest {
    private UUID routeId;
}
