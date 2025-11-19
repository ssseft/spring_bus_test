package com.example.bustest.dto.driving;

import com.example.bustest.domain.bus.Driving;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrivingResponse {
    private UUID id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    private String status;
    private UUID routeId;
    private Instant createdAt;
    private Instant updatedAt;

    public static DrivingResponse from(Driving r) {
        UUID routeId = r.getRoute() != null ? r.getRoute().getId() : null;
        return new DrivingResponse(
                r.getId(),
                r.getDate(),
                r.getStatus() != null ? r.getStatus().toString() : null,
                routeId,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

