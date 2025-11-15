package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.Schedule;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class ScheduleWithStudentsRequest {
    private UUID academyId;
    private UUID routeId;
    private String name;
    private Integer repeatDays;
    private LocalTime startTime;
    private Schedule.BoardingStatus boardingStatus;
    private List<ScheduleStudentRequest> assignments;
}

