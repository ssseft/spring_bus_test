package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.Route;
import com.example.bustest.domain.bus.ScheduleDailyPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanResponse {
    private UUID id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    private String status;
    private UUID routeId;
    private Instant createdAt;
    private Instant updatedAt;

    public static PlanResponse from(ScheduleDailyPlan p) {
        UUID routeId = p.getRoute() != null ? p.getRoute().getId() : null;
        return new PlanResponse(
                p.getId(),
                p.getDate(),
                p.getStatus().name(),
                routeId,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanStatusUpdateRequest {
    private ScheduleDailyPlan.Status status; // OPER or CANCELED
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanUpsertRequest {
    private ScheduleDailyPlan.Status status; // optional
    private UUID routeId; // optional
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanBulkRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> dates;
    private ScheduleDailyPlan.Status status; // optional: set same status for all
}
