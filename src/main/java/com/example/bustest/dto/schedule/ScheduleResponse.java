package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.Schedule;
import com.example.bustest.domain.bus.ScheduleStudent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
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
        List<Assignment> assigns = list == null ? java.util.List.of() : list.stream()
                .map(ss -> new Assignment(ss.getStudent().getId(), ss.getBusStop().getId()))
                .toList();
        return new ScheduleResponse(
                s.getId(),
                s.getAcademyId(),
                s.getRoute() != null ? s.getRoute().getId() : null,
                s.getName(),
                s.getRepeatDays(),
                s.getStartTime(),
                s.getEndTime(),
                s.getBoardingStatus() != null ? s.getBoardingStatus().name() : null,
                s.getIsActive(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                assigns
        );
    }
}

