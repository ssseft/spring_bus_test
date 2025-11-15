package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.RunService;
import com.example.bustest.Service.bus.RunStudentService;
import com.example.bustest.dto.run.RunStudentCancelResponse;
import com.example.bustest.domain.bus.Run;
import com.example.bustest.domain.bus.RunStudent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/runs/{runId}/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RunStudentController {

    private final RunStudentService runStudentService;
    private final RunService runService;

    // PATCH /api/runs/{runId}/students/{studentId}/cancel
    @PatchMapping("/{studentId}/cancel")
    public ResponseEntity<RunStudentCancelResponse> cancelAndMaybeRebuild(
            @PathVariable UUID runId,
            @PathVariable UUID studentId
    ) {
        boolean rebuilt = runStudentService.cancelAndMaybeRebuildRoute(runId, studentId);
        Run run = runService.get(runId);
        UUID routeId = run.getRoute() != null ? run.getRoute().getId() : null;
        RunStudentCancelResponse body = RunStudentCancelResponse.of(runId, studentId, RunStudent.RunStudentStatus.canceled, routeId, rebuilt);
        return ResponseEntity.ok(body);
    }
}
