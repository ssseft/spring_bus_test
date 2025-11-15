package com.example.bustest.dto.run;

import com.example.bustest.domain.bus.Run;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RunUpsertRequest {
    private Run.RunStatus status; // optional
    private UUID routeId; // optional
}

