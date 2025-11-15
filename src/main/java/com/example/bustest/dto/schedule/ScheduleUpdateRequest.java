package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.Schedule;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class ScheduleUpdateRequest {
    private UUID routeId;
    private String name;
    private Integer repeatDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private Schedule.BoardingStatus boardingStatus;
    private Boolean isActive;
}

