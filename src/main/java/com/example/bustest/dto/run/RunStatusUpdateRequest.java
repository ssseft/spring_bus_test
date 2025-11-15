package com.example.bustest.dto.run;

import com.example.bustest.domain.bus.Run;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RunStatusUpdateRequest {
    private Run.RunStatus status; // scheduled / in_progress / completed / canceled
}
