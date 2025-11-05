package com.example.bustest.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * 노선 상세 조회 응답 DTO.
 * - 사용처: GET /api/routes/{routeId}
 * - 내용: 식별자, 이름, 총시간(초) + 경로 요약(path/distance/duration)
 */

// 사실상 Summary가 Detail에 포함되는거라 구분을 같이 해서
// Service에서 Summary호출할 때 Detail에서 필요한 필드만 가져와도 되긴 하는데 나중에라도 필요할까봐 구분해 놓았음.

// 또한 RoutePathResponse를 여기에 합칠까 생각해보았지만, 운행 부분까지 구현해보고 결정하는게 나을 것 같아 구분해놓음.
@Getter
@AllArgsConstructor
public class RouteDetailResponse {
    private final UUID id;
    private final String name;
    private final long totalTimeSeconds;
    private final long distanceMeters;
    private final long durationSeconds;
    private final List<CoordinateDTO> path;
}