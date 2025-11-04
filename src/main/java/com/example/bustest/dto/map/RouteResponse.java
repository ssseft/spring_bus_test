package com.example.bustest.dto.map;

import java.util.List;

// 길찾기(노선) 결과를 화면에 돌려주기 위한 응답 DTO 입니다.
// - path: 지도에 그릴 경로 점들(위도/경도 순)
// - distanceMeters: 총 이동 거리(미터)
// - durationSeconds: 총 소요 시간(초)
public class RouteResponse {
    private List<CoordinateDTO> path;
    private long distanceMeters;
    private long durationSeconds;

    public RouteResponse() {}

    public RouteResponse(List<CoordinateDTO> path, long distanceMeters, long durationSeconds) {
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

