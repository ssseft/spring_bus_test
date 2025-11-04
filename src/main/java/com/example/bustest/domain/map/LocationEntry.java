package com.example.bustest.domain.map;

import jakarta.persistence.*;

// 위치 엔티티: 제목, 주소, 좌표(위도/경도), 유형(STUDENT/STOP)을 보관합니다.
@Entity
@Table(name = "location_entries")
public class LocationEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String address;

    // Kakao 응답은 y=위도, x=경도이므로 명확히 구분해 보관합니다.
    private Double latitude;   // 위도(y)
    private Double longitude;  // 경도(x)

    @Column(nullable = false)
    private String type; // STUDENT 또는 STOP

    public LocationEntry() {}

    public LocationEntry(String title, String address, Double latitude, Double longitude, String type) {
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

