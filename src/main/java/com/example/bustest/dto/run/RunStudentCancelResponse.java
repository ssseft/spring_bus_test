package com.example.bustest.dto.run;

import com.example.bustest.domain.bus.RunStudent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RunStudentCancelResponse {
    private UUID runId;
    private UUID studentId;
    private String status; // canceled
    private UUID routeId;  // current route after potential rebuild
    private boolean routeRebuilt;

    public static RunStudentCancelResponse of(UUID runId, UUID studentId, RunStudent.RunStudentStatus status, UUID routeId, boolean rebuilt) {
        return new RunStudentCancelResponse(runId, studentId, status != null ? status.name() : null, routeId, rebuilt);
    }
}

