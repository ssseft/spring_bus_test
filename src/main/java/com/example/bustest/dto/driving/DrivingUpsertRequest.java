package com.example.bustest.dto.driving;

import com.example.bustest.domain.bus.Driving;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrivingUpsertRequest {
    private Driving.drivingStatus status; // optional
    private UUID routeId; // optional
}

