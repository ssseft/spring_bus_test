package com.example.bustest.dto.bus;

import com.example.bustest.domain.bus.BusStop;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// 조회용 dto
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BusStopSummaryResponse {
    private UUID id;
    private UUID academyId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String photoUrl;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    //생성자
    public static BusStopSummaryResponse from(BusStop b) {
        return BusStopSummaryResponse.builder()
                .id(b.getId())
                .academyId(b.getAcademyId())
                .name(b.getName())
                .latitude(b.getLatitude())
                .longitude(b.getLongitude())
                .photoUrl(b.getPhotoUrl())
                .isActive(b.getIsActive())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}