package com.example.bustest.dto.bus;

import com.example.bustest.domain.bus.BusStop;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 정류장 생성 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusStopCreateRequest {

    /** 소속 학원 ID (필수) */
    @NotNull(message = "academyId는 필수입니다.")
    private UUID academyId;

    @NotBlank(message = "정류장 이름은 필수입니다.")
    @Size(max = 255, message = "정류장 이름은 최대 255자입니다.")
    private String name;

    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "-90.0", inclusive = true, message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", inclusive = true, message = "위도는 90 이하이어야 합니다.")
    private BigDecimal latitude;   // DECIMAL(10,8)

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "-180.0", inclusive = true, message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", inclusive = true, message = "경도는 180 이하이어야 합니다.")
    private BigDecimal longitude;  // DECIMAL(11,8)

    @Size(max = 500, message = "사진 URL은 최대 500자입니다.")
    private String photoUrl;

    private Boolean isActive;

    //생성자
    public BusStop toEntity() {
        return BusStop.builder()
                .academyId(this.academyId)
                .name(this.name)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .photoUrl(this.photoUrl)
                .isActive(this.isActive == null ? Boolean.TRUE : this.isActive)
                .build();
    }
}
