package com.example.bustest.dto.map;

import lombok.Getter;
import lombok.Setter;

// 위치 수정 요청 DTO
// - 제목(title)과 유형(type)을 부분 업데이트 할 수 있도록 선택 입력(Optional)으로 둡니다.
@Setter
@Getter
public class LocationUpdateRequest {
    private String title; // 
    private String type;  //  STUDENT or STOP // enum대체 가능
}

