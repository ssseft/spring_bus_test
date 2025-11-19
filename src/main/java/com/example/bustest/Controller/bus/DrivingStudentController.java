package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.DrivingService;
import com.example.bustest.Service.bus.DrivingStudentService;
import com.example.bustest.dto.driving.DrivingStudentCancelResponse;
import com.example.bustest.domain.bus.Driving;
import com.example.bustest.domain.bus.DrivingStudent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/drivings/{drivingId}/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DrivingStudentController {

    private final DrivingStudentService drivingStudentService;
    private final DrivingService drivingService;

    // PATCH /api/drivings/{drivingId}/students/{studentId}/cancel
    @PatchMapping("/{studentId}/cancel")
    public ResponseEntity<DrivingStudentCancelResponse> cancelAndMaybeRebuild(
            @PathVariable UUID drivingId,
            @PathVariable UUID studentId
    ) {
        boolean rebuilt = drivingStudentService.cancelAndMaybeRebuildRoute(drivingId, studentId);
        Driving driving = drivingService.get(drivingId);
        UUID routeId = driving.getRoute() != null ? driving.getRoute().getId() : null;
        DrivingStudentCancelResponse body = DrivingStudentCancelResponse.of(drivingId, studentId, DrivingStudent.drivingStudentStatus.canceled, routeId, rebuilt);
        return ResponseEntity.ok(body);
    }
}
