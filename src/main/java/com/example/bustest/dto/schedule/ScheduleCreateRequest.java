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
    
    //schedule생성 시 반드시 schedulestudent도 생성
    // 따라서 student list도 같이 저장 후 요청
    private List<ScheduleStudentRequest> assignments;
}

