package com.example.bustest.dto.map;

import lombok.Value;
import java.util.List;

/**
 * 길찾기(노선) 경로 요약 응답 DTO.
 * - 사용처: 프리뷰 응답, 생성 응답 내 summary, 상세 파싱 결과 일부.
 * - path: 지도에 그릴 경로 점들(위도/경도 순)
 * - distanceMeters: 총 이동 거리(미터)
 * - durationSeconds: 총 소요 시간(초)
 */
@Value
public class RoutePathResponse {
    List<CoordinateDTO> path;
    long distanceMeters;
    long durationSeconds;
}