package com.example.bustest.dto.map;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 노선 등록을 위해 화면에서 선택한 정류장(마커) id 목록을
// 순서대로 전달받는 요청 DTO 입니다.
// - orderedStopIds: 사용자가 선택한 순서대로 담겨 있습니다.
@Setter
@Getter
public class RouteRequest {
    private List<Long> orderedStopIds;
}

