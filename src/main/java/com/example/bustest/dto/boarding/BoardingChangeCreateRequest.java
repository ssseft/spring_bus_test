package com.example.bustest.dto.boarding;

import lombok.Data;

import java.util.UUID;

@Data
public class BoardingChangeCreateRequest {
    private UUID toBusStopId;
    private String reason;
}

