package com.example.bustest.dto.map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LocationMarkerDTO {
    private Long id;
    private String title;
    private double lat;
    private double lng;
    private String type;

    public LocationMarkerDTO(Long id, String title, double lat, double lng, String type) {
        this.id = id;
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.type = type;
    }

}
