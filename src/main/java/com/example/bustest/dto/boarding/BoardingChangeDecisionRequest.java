package com.example.bustest.dto.boarding;

import lombok.Data;

import java.util.UUID;

@Data
public class BoardingChangeDecisionRequest {
    private UUID processedBy;
    private String rejectReason;
}

