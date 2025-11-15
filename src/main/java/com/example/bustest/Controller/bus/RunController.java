package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.RunService;
import com.example.bustest.dto.run.RunBulkRequest;
import com.example.bustest.dto.run.RunResponse;
import com.example.bustest.dto.run.RunStatusUpdateRequest;
import com.example.bustest.dto.run.RunUpsertRequest;
import com.example.bustest.domain.bus.Run;
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
@RequestMapping("/api/schedules/{scheduleId}/runs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RunController {

    private final RunService runService;

    // POST /runs:build?from&to  → upsertRange then list
    @PostMapping(":build")
    public List<RunResponse> build(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        runService.upsertRange(scheduleId, from, to);
        return runService.list(scheduleId, from, to).stream().map(RunResponse::from).toList();
    }

    // GET /runs?from&to → list
    @GetMapping
    public List<RunResponse> list(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return runService.list(scheduleId, from, to).stream().map(RunResponse::from).toList();
    }

    // PUT /runs/{date} → ensure exists and optionally set status/route
    @PutMapping("/{date}")
    public RunResponse upsert(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody(required = false) RunUpsertRequest body
    ) {
        Run run = runService.setStatus(scheduleId, date, false);
        if (body != null) {
            if (body.getStatus() != null) {
                boolean canceled = body.getStatus() == Run.RunStatus.canceled;
                run = runService.setStatus(scheduleId, date, canceled);
            }
            if (body.getRouteId() != null) {
                run = runService.overrideRoute(scheduleId, date, body.getRouteId());
            }
        }
        return RunResponse.from(run);
    }

    // PATCH /runs/{date}/status → set status
    @PatchMapping("/{date}/status")
    public RunResponse updateStatus(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody RunStatusUpdateRequest req
    ) {
        boolean canceled = req.getStatus() == Run.RunStatus.canceled;
        Run run = runService.setStatus(scheduleId, date, canceled);
        return RunResponse.from(run);
    }

    // DELETE /runs/{date} → delete if safe else set CANCELED
    @DeleteMapping("/{date}")
    public ResponseEntity<?> delete(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        boolean deleted = runService.deleteOrCancel(scheduleId, date);
        if (deleted) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        Run run = runService.setStatus(scheduleId, date, true);
        return ResponseEntity.ok(RunResponse.from(run));
    }

    // POST /runs:bulk → dates upsert and optional status apply
    @PostMapping(":bulk")
    public List<RunResponse> bulk(
            @PathVariable UUID scheduleId,
            @RequestBody RunBulkRequest req
    ) {
        List<RunResponse> out = new ArrayList<>();
        if (req.getDates() == null) return out;
        for (LocalDate d : req.getDates()) {
            Run run = runService.setStatus(scheduleId, d, false);
            if (req.getStatus() != null) {
                boolean canceled = req.getStatus() == Run.RunStatus.canceled;
                run = runService.setStatus(scheduleId, d, canceled);
            }
            out.add(RunResponse.from(run));
        }
        return out;
    }
}
