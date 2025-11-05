package com.example.bustest.Controller.bus;

import com.example.bustest.dto.bus.BusStopCreateRequest;
import com.example.bustest.dto.bus.BusStopSummaryResponse;
import com.example.bustest.dto.bus.BusStopUpdateRequest;
import com.example.bustest.Service.bus.BusStopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/busstop") // 매핑명은 일단 임의로 설정했음
@CrossOrigin(origins = "*")

public class BusStopController {



    private final BusStopService busStopService;

    // 학원별 정류장 조회
    @GetMapping("/academies/{academyId}/bus-stops")
    public Page<BusStopSummaryResponse> list(@PathVariable UUID academyId, Pageable pageable) {
        return busStopService.pageByAcademy(academyId, pageable);
    }

    // 정류장 생성
    @PostMapping("/academies/{academyId}/bus-stops")
    @ResponseStatus(HttpStatus.CREATED)
    public BusStopCreateRequest create(@PathVariable UUID academyId, @Valid @RequestBody BusStopCreateRequest busReq) {
        busReq.setAcademyId(academyId);
        BusStopSummaryResponse saved = busStopService.create(busReq);
        return BusStopCreateRequest.builder()
                .academyId(saved.getAcademyId())
                .name(saved.getName())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .photoUrl(saved.getPhotoUrl())
                .isActive(saved.getIsActive())
                .build();
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
