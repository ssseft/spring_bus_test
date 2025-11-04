package com.example.bustest.dto.map;


import lombok.Getter;

@Getter
public class CoordinateDTO {
    private final double latitude;  // y
    private final double longitude; // x

    public CoordinateDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

