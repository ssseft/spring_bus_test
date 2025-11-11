package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.ScheduleDailyPlanService;
import com.example.bustest.dto.schedule.PlanBulkRequest;
import com.example.bustest.dto.schedule.PlanResponse;
import com.example.bustest.dto.schedule.PlanStatusUpdateRequest;
import com.example.bustest.dto.schedule.PlanUpsertRequest;
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
    public List<PlanResponse> build(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        dailyPlanService.upsertRange(scheduleId, from, to);
        return dailyPlanService.list(scheduleId, from, to).stream().map(PlanResponse::from).toList();
    }

    // GET /plans?from&to → list
    @GetMapping
    public List<PlanResponse> list(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return dailyPlanService.list(scheduleId, from, to).stream().map(PlanResponse::from).toList();
    }

    // PUT /plans/{date} → ensure exists and optionally set status/route
    @PutMapping("/{date}")
    public PlanResponse upsert(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody(required = false) PlanUpsertRequest body
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
        return PlanResponse.from(plan);
    }

    // PATCH /plans/{date}/status → toggle OPER/CANCELED
    @PatchMapping("/{date}/status")
    public PlanResponse updateStatus(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody PlanStatusUpdateRequest req
    ) {
        boolean canceled = req.getStatus() == ScheduleDailyPlan.Status.CANCELED;
        ScheduleDailyPlan plan = dailyPlanService.setNoService(scheduleId, date, canceled);
        return PlanResponse.from(plan);
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
        return ResponseEntity.ok(PlanResponse.from(plan));
    }

    // POST /plans:bulk → dates upsert and optional status apply
    @PostMapping(":bulk")
    public List<PlanResponse> bulk(
            @PathVariable UUID scheduleId,
            @RequestBody PlanBulkRequest req
    ) {
        List<PlanResponse> out = new ArrayList<>();
        if (req.getDates() == null) return out;
        for (LocalDate d : req.getDates()) {
            ScheduleDailyPlan plan = dailyPlanService.setNoService(scheduleId, d, false);
            if (req.getStatus() != null) {
                boolean canceled = req.getStatus() == ScheduleDailyPlan.Status.CANCELED;
                plan = dailyPlanService.setNoService(scheduleId, d, canceled);
            }
            out.add(PlanResponse.from(plan));
        }
        return out;
    }
}
