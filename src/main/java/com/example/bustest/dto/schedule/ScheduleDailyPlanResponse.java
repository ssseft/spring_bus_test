package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.ScheduleDailyPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDailyPlanResponse {
    private UUID id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    private String status;
    private UUID routeId;
    private Instant createdAt;
    private Instant updatedAt;

    public static ScheduleDailyPlanResponse from(ScheduleDailyPlan p) {
        UUID routeId = p.getRoute() != null ? p.getRoute().getId() : null;
        return new ScheduleDailyPlanResponse(
                p.getId(),
                p.getDate(),
                p.getStatus().name(),
                routeId,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
