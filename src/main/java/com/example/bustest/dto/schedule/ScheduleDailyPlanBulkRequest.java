package com.example.bustest.dto.schedule;

import com.example.bustest.domain.bus.ScheduleDailyPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDailyPlanBulkRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> dates;
    private ScheduleDailyPlan.Status status; // optional: set same status for all
}
