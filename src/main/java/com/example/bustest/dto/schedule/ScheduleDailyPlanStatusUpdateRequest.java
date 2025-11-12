package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.ScheduleDailyPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDailyPlanStatusUpdateRequest {
    private ScheduleDailyPlan.Status status; // OPER or CANCELED
}
