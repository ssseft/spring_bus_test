package com.example.bustest.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * 노선 생성 결과 DTO.
 * - 사용처: POST /api/routes/academies/{academyId}
 * - 내용: 생성된 routeId와 지도 표시용 요약(RoutePathResponse)
 */
@Getter
@AllArgsConstructor
public class RouteCreateResponse {
    private final UUID routeId;
    private final RoutePathResponse summary;
}
