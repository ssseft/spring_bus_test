package com.example.bustest.dto.map;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LocationCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String address;
    @NotBlank
    private String type; // STUDENT or STOP
}

