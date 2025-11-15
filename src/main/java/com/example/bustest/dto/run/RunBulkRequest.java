package com.example.bustest.dto.run;

import com.example.bustest.domain.bus.Run;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RunBulkRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> dates;
    private Run.RunStatus status; // optional: set same status for all
}

