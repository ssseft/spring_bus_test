package com.example.bustest.dto.driving;

import com.example.bustest.domain.bus.DrivingStudent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrivingStudentCancelResponse {
    private UUID drivingId;
    private UUID studentId;
    private String status; // canceled
    private UUID routeId;  // current route after potential rebuild
    private boolean routeRebuilt;

    public static DrivingStudentCancelResponse of(UUID drivingId, UUID studentId, DrivingStudent.drivingStudentStatus status, UUID routeId, boolean rebuilt) {
        return new DrivingStudentCancelResponse(drivingId, studentId, status != null ? status.name() : null, routeId, rebuilt);
    }
}

