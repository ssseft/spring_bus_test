package com.example.bustest.dto.bus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 주소 기반 정류장 생성 요청 DTO
 * - 서버에서 카카오 지오코딩으로 좌표를 조회하여 BusStop을 생성합니다.
 * 이 dto로 주소 -> Point로 변환하기 위한 사전작업 -> 이후 BusStopCreateRequest에 Point로 넣음
 * >> 분리가 애매함 > 일단 놔두고 운행 전까지 하고
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusStopAddressCreateRequest {
    @NotBlank(message = "정류장 이름은 필수입니다.")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 500)
    private String address;

    @Size(max = 500)
    private String photoUrl;

    private Boolean isActive;
}