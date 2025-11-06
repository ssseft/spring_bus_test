package com.example.bustest.dto.map;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * 노선 생성/프리뷰 공용 요청 DTO.
 * - 사용처: POST /api/routes/academies/{academyId}, POST /api/routes/preview
 * - 비고: 프리뷰에서는 name을 사용하지 않고 정류장 순서만 사용.
 *  dto는 줄이기는 했지만, 기존 필드의 재사용을 고집하지는 않았음.
 *  -> 나중에 추가 할 수도 있기 때문에
 */
@Getter
@Setter
public class RouteCreateRequest {
    private String name;
    private List<UUID> orderedBusStopIds;
}