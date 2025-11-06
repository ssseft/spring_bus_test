package com.example.bustest.Controller.bus;

import com.example.bustest.dto.bus.BusStopCreateRequest;
import com.example.bustest.dto.bus.BusStopSummaryResponse;
import com.example.bustest.dto.bus.BusStopUpdateRequest;
import com.example.bustest.dto.bus.BusStopAddressCreateRequest;
import com.example.bustest.Service.bus.BusStopService;
import com.example.bustest.Service.map.KakaoApiService;
import com.example.bustest.dto.map.CoordinateDTO;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/busstop") // 매핑명은 일단 임의로 설정했음
@CrossOrigin(origins = "*")

public class BusStopController {

    private final BusStopService busStopService;
    private final KakaoApiService kakaoApiService;

    // 학원별 정류장 조회
    @GetMapping("/academies/{academyId}/bus-stops")
    public Page<BusStopSummaryResponse> list(@PathVariable UUID academyId, Pageable pageable) {
        return busStopService.pageByAcademy(academyId, pageable);
    }

    // 정류장 생성
    @PostMapping("/academies/{academyId}/bus-stops")
    public ResponseEntity<BusStopSummaryResponse> create(@PathVariable UUID academyId, @Valid @RequestBody BusStopCreateRequest req) {
        req.setAcademyId(academyId);
        BusStopSummaryResponse saved = busStopService.create(req);
        return ResponseEntity.created(URI.create("/api/busstop/bus-stops/" + saved.getId()))
                .body(saved);
    }

    // 주소 기반 정류장 생성 (서버 지오코딩)
    @PostMapping("/academies/{academyId}/bus-stops/geocode")
    public ResponseEntity<BusStopSummaryResponse> createByAddress(@PathVariable UUID academyId,
                                                                  @Valid @RequestBody BusStopAddressCreateRequest req) {
        var coordOpt = kakaoApiService.geocodeWithFallback(req.getAddress());
        CoordinateDTO c = coordOpt.orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_DESTINATION_NOT_FOUND));

        BusStopCreateRequest createReq = BusStopCreateRequest.builder()
                .academyId(academyId)
                .name(req.getName())
                .latitude(java.math.BigDecimal.valueOf(c.getLatitude()))
                .longitude(java.math.BigDecimal.valueOf(c.getLongitude()))
                .photoUrl(req.getPhotoUrl())
                .isActive(req.getIsActive())
                .build();
        BusStopSummaryResponse saved = busStopService.create(createReq);
        return ResponseEntity.created(URI.create("/api/busstop/bus-stops/" + saved.getId()))
                .body(saved);
    }


    //수정
    @PatchMapping("/bus-stops/{id}")
    public BusStopSummaryResponse update(@PathVariable UUID id, @Valid @RequestBody BusStopUpdateRequest busReq) {
        return busStopService.update(id, busReq);
    }

    //삭제
    @DeleteMapping("/bus-stops/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        busStopService.delete(id);
    }
}