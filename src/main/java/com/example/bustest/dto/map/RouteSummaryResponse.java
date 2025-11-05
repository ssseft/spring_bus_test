package com.example.bustest.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 학원별 노선 목록 조회용 요약 DTO.
 * - 사용처: GET /api/routes/academies/{academyId}
 * - 목적: 리스트 화면에 가볍게 노출(id, name, totalTimeSeconds, createdAt)
 */
@Getter
@AllArgsConstructor
public class RouteSummaryResponse {
    private final UUID id;
    private final String name;
    private final long totalTimeSeconds;
    private final Instant createdAt;
}
