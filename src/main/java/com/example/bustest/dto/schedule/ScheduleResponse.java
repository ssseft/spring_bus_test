package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.Schedule;
import com.example.bustest.domain.bus.ScheduleStudent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponse {
    private UUID id;
    private UUID academyId;
    private UUID routeId;
    private String name;
    private Integer repeatDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private String boardingStatus;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private List<Assignment> assignments;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Assignment {
        private UUID studentId;
        private UUID busStopId;
    }

    public static ScheduleResponse from(Schedule s, List<ScheduleStudent> list) {
        List<Assignment> assigns = new ArrayList<>();

        // 가독성을 위해 이렇게 작성 ScheduleStudent -> Assignment로 바꾸는 과정
        for (ScheduleStudent ss : list) {
            UUID studentId = ss.getStudent().getId();
            UUID busStopId = ss.getBusStop().getId();
            assigns.add(new Assignment(studentId, busStopId));
        }


        return new ScheduleResponse(
                s.getId(),
                s.getAcademyId(),
                s.getRoute() == null ?  null : s.getRoute().getId(),
                s.getName(),
                s.getRepeatDays(),
                s.getStartTime(),
                s.getEndTime(),
                s.getBoardingStatus() == null ? null : s.getBoardingStatus().name() ,
                s.getIsActive(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                assigns
        );
    }
}

