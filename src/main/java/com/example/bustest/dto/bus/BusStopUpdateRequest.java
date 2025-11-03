package com.example.bustest.dto.bus;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

// 수정 dto
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusStopUpdateRequest {

    @Size(max = 255)
    private String name;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Size(max = 500)
    private String photoUrl;

    private Boolean isActive;
}
