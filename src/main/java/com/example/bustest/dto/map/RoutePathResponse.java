package com.example.bustest.dto.map;

import java.util.List;

/**
 * 길찾기(노선) 경로 요약 응답 DTO.
 * - 사용처: 프리뷰 응답, 생성 응답 내 summary, 상세 파싱 결과 일부.
 * - path: 지도에 그릴 경로 점들(위도/경도 순)
 * - distanceMeters: 총 이동 거리(미터)
 * - durationSeconds: 총 소요 시간(초)
 */
public class RoutePathResponse {
    private List<CoordinateDTO> path;
    private long distanceMeters;
    private long durationSeconds;

    public RoutePathResponse() {}

    public RoutePathResponse(List<CoordinateDTO> path, long distanceMeters, long durationSeconds) {
        this.path = path;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
    }

    public List<CoordinateDTO> getPath() { return path; }
    public void setPath(List<CoordinateDTO> path) { this.path = path; }
    public long getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(long distanceMeters) { this.distanceMeters = distanceMeters; }
    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
}
