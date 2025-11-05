package com.example.bustest.dto.map;


import lombok.Getter;


//위도,경도 dto 반드시 필요 -> 수정x
/**
 * 지도 좌표 DTO(위도/경도).
 * - 사용처: 길찾기 요청/응답의 경로 좌표 표현.
 * - 비고: Kakao Navi는 x=경도(lng), y=위도(lat) 순서를 사용.
 */
@Getter
public class CoordinateDTO {
    private final double latitude;
    private final double longitude;

    public CoordinateDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

