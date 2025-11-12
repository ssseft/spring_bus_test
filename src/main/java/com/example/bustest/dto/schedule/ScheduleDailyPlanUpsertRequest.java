package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.ScheduleDailyPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDailyPlanUpsertRequest {
    private ScheduleDailyPlan.Status status; // optional
    private UUID routeId; // optional
}
