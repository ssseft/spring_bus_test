package com.example.bustest.dto.boarding;

import com.example.bustest.domain.bus.BoardingChangeRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardingChangeResponse {
    private UUID id;
    private UUID runId;
    private UUID studentId;
    private UUID fromStopId;
    private UUID toStopId;
    private String status;
    private String reason;
    private String rejectReason;
    private UUID processedBy;
    private Instant requestedAt;
    private Instant processedAt;

    public static BoardingChangeResponse from(BoardingChangeRequest r) {
        return new BoardingChangeResponse(
                r.getId(),
                r.getRun().getId(),
                r.getStudent().getId(),
                r.getFromStop().getId(),
                r.getToStop().getId(),
                r.getStatus().name(),
                r.getReason(),
                r.getRejectReason(),
                r.getProcessedBy(),
                r.getRequestedAt(),
                r.getProcessedAt()
        );
    }
}

