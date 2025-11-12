package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.ScheduleDailyPlanService;
import com.example.bustest.dto.schedule.ScheduleDailyPlanBulkRequest;
import com.example.bustest.dto.schedule.ScheduleDailyPlanResponse;
import com.example.bustest.dto.schedule.ScheduleDailyPlanStatusUpdateRequest;
import com.example.bustest.dto.schedule.ScheduleDailyPlanUpsertRequest;
import com.example.bustest.domain.bus.ScheduleDailyPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules/{scheduleId}/plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchedulePlanController {

    private final ScheduleDailyPlanService dailyPlanService;

    // POST /plans:build?from&to  → upsertRange then list
    @PostMapping(":build")
    public List<ScheduleDailyPlanResponse> build(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        dailyPlanService.upsertRange(scheduleId, from, to);
        return dailyPlanService.list(scheduleId, from, to).stream().map(ScheduleDailyPlanResponse::from).toList();
    }

    // GET /plans?from&to → list
    @GetMapping
    public List<ScheduleDailyPlanResponse> list(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return dailyPlanService.list(scheduleId, from, to).stream().map(ScheduleDailyPlanResponse::from).toList();
    }

    // PUT /plans/{date} → ensure exists and optionally set status/route
    @PutMapping("/{date}")
    public ScheduleDailyPlanResponse upsert(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody(required = false) ScheduleDailyPlanUpsertRequest body
    ) {
        // ensure exists
        ScheduleDailyPlan plan = dailyPlanService.setNoService(scheduleId, date, false);
        if (body != null) {
            if (body.getStatus() != null) {
                boolean cancel = body.getStatus() == ScheduleDailyPlan.Status.CANCELED;
                plan = dailyPlanService.setNoService(scheduleId, date, cancel);
            }
            if (body.getRouteId() != null) {
                plan = dailyPlanService.overrideRoute(scheduleId, date, body.getRouteId());
            }
        }
        return ScheduleDailyPlanResponse.from(plan);
    }

    // PATCH /plans/{date}/status → toggle OPER/CANCELED
    @PatchMapping("/{date}/status")
    public ScheduleDailyPlanResponse updateStatus(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody ScheduleDailyPlanStatusUpdateRequest req
    ) {
        boolean canceled = req.getStatus() == ScheduleDailyPlan.Status.CANCELED;
        ScheduleDailyPlan plan = dailyPlanService.setNoService(scheduleId, date, canceled);
        return ScheduleDailyPlanResponse.from(plan);
    }

    // DELETE /plans/{date} → delete if safe, else 200 with CANCELED current plan
    @DeleteMapping("/{date}")
    public ResponseEntity<?> delete(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        boolean deleted = dailyPlanService.deleteOrCancel(scheduleId, date);
        if (deleted) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        // CANCELED applied; fetch current
        ScheduleDailyPlan plan = dailyPlanService.setNoService(scheduleId, date, true);
        return ResponseEntity.ok(ScheduleDailyPlanResponse.from(plan));
    }

    // POST /plans:bulk → dates upsert and optional status apply
    @PostMapping(":bulk")
    public List<ScheduleDailyPlanResponse> bulk(
            @PathVariable UUID scheduleId,
            @RequestBody ScheduleDailyPlanBulkRequest req
    ) {
        List<ScheduleDailyPlanResponse> out = new ArrayList<>();
        if (req.getDates() == null) return out;
        for (LocalDate d : req.getDates()) {
            ScheduleDailyPlan plan = dailyPlanService.setNoService(scheduleId, d, false);
            if (req.getStatus() != null) {
                boolean canceled = req.getStatus() == ScheduleDailyPlan.Status.CANCELED;
                plan = dailyPlanService.setNoService(scheduleId, d, canceled);
            }
            out.add(ScheduleDailyPlanResponse.from(plan));
        }
        return out;
    }
}
