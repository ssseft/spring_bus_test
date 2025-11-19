package com.example.bustest.dto.driving;

import com.example.bustest.domain.bus.Driving;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrivingStatusUpdateRequest {
    private Driving.drivingStatus status; // scheduled / in_progress / completed / canceled
}
