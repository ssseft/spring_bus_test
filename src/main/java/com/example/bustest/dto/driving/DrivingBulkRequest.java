package com.example.bustest.dto.driving;

import com.example.bustest.domain.bus.Driving;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrivingBulkRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> dates;
    private Driving.drivingStatus status; // optional: set same status for all
}

