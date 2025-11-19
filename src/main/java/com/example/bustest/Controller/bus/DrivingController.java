package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.DrivingService;
import com.example.bustest.dto.driving.DrivingBulkRequest;
import com.example.bustest.dto.driving.DrivingResponse;
import com.example.bustest.dto.driving.DrivingStatusUpdateRequest;
import com.example.bustest.dto.driving.DrivingUpsertRequest;
import com.example.bustest.domain.bus.Driving;
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
@RequestMapping("/api/schedules/{scheduleId}/drivings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DrivingController {

    private final DrivingService drivingService;

    // POST /drivings:build?from&to  → upsertRange then list
    @PostMapping(":build")
    public List<DrivingResponse> build(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        drivingService.upsertRange(scheduleId, from, to);
        return drivingService.list(scheduleId, from, to).stream().map(DrivingResponse::from).toList();
    }

    // GET /drivings?from&to → list
    @GetMapping
    public List<DrivingResponse> list(
            @PathVariable UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return drivingService.list(scheduleId, from, to).stream().map(DrivingResponse::from).toList();
    }

    // PUT /drivings/{date} → ensure exists and optionally set status/route
    @PutMapping("/{date}")
    public DrivingResponse upsert(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody(required = false) DrivingUpsertRequest body
    ) {
        Driving driving = drivingService.setStatus(scheduleId, date, false);
        if (body != null) {
            if (body.getStatus() != null) {
                boolean canceled = body.getStatus() == Driving.drivingStatus.canceled;
                driving = drivingService.setStatus(scheduleId, date, canceled);
            }
            if (body.getRouteId() != null) {
                driving = drivingService.overrideRoute(scheduleId, date, body.getRouteId());
            }
        }
        return DrivingResponse.from(driving);
    }

    // PATCH /drivings/{date}/status → set status
    @PatchMapping("/{date}/status")
    public DrivingResponse updateStatus(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody DrivingStatusUpdateRequest req
    ) {
        boolean canceled = req.getStatus() == Driving.drivingStatus.canceled;
        Driving driving = drivingService.setStatus(scheduleId, date, canceled);
        return DrivingResponse.from(driving);
    }

    // DELETE /drivings/{date} → delete if safe else set CANCELED
    @DeleteMapping("/{date}")
    public ResponseEntity<?> delete(
            @PathVariable UUID scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        boolean deleted = drivingService.deleteOrCancel(scheduleId, date);
        if (deleted) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        Driving driving = drivingService.setStatus(scheduleId, date, true);
        return ResponseEntity.ok(DrivingResponse.from(driving));
    }

    // POST /drivings:bulk → dates upsert and optional status apply
    @PostMapping(":bulk")
    public List<DrivingResponse> bulk(
            @PathVariable UUID scheduleId,
            @RequestBody DrivingBulkRequest req
    ) {
        List<DrivingResponse> out = new ArrayList<>();
        if (req.getDates() == null) return out;
        for (LocalDate d : req.getDates()) {
            Driving driving = drivingService.setStatus(scheduleId, d, false);
            if (req.getStatus() != null) {
                boolean canceled = req.getStatus() == Driving.drivingStatus.canceled;
                driving = drivingService.setStatus(scheduleId, d, canceled);
            }
            out.add(DrivingResponse.from(driving));
        }
        return out;
    }
}
