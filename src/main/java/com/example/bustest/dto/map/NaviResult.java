package com.example.bustest.dto.map;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NaviResult {
    private final RouteResponse summary;
    private final JsonNode raw;
}

