package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.Schedule;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;
import java.util.List;

@Data
public class ScheduleCreateRequest {
    private UUID academyId;
    private UUID routeId;
    private String name;
    private Integer repeatDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private Schedule.BoardingStatus boardingStatus;
    private Boolean isActive;
    // optional: 함께 생성할 학생 배정 목록
    private List<ScheduleStudentRequest> assignments;
}
