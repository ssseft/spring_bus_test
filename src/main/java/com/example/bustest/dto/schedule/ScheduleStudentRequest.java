package com.example.bustest.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleStudentRequest {
    private UUID studentId;
    private UUID busStopId;
}
