package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.BoardingChangeRequestService;
import com.example.bustest.dto.boarding.BoardingChangeCreateRequest;
import com.example.bustest.dto.boarding.BoardingChangeDecisionRequest;
import com.example.bustest.dto.boarding.BoardingChangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BoardingChangeRequestController {

    private final BoardingChangeRequestService service;

    // 학생 요청 생성: POST /api/runs/{runId}/students/{studentId}/boarding-change-requests
    @PostMapping("/api/runs/{runId}/students/{studentId}/boarding-change-requests")
    public BoardingChangeResponse create(@PathVariable UUID runId,
                                         @PathVariable UUID studentId,
                                         @RequestBody BoardingChangeCreateRequest req) {
        var r = service.request(runId, studentId, req.getToBusStopId(), req.getReason());
        return BoardingChangeResponse.from(r);
    }

    // 학원 승인: POST /api/boarding-change-requests/{id}:approve
    @PostMapping("/api/boarding-change-requests/{id}:approve")
    public BoardingChangeResponse approve(@PathVariable UUID id,
                                          @RequestBody BoardingChangeDecisionRequest body) {
        var r = service.approve(id, body.getProcessedBy());
        return BoardingChangeResponse.from(r);
    }

    // 학원 반려: POST /api/boarding-change-requests/{id}:reject
    @PostMapping("/api/boarding-change-requests/{id}:reject")
    public BoardingChangeResponse reject(@PathVariable UUID id,
                                         @RequestBody BoardingChangeDecisionRequest body) {
        var r = service.reject(id, body.getProcessedBy(), body.getRejectReason());
        return BoardingChangeResponse.from(r);
    }

    // Run 기준 목록: GET /api/runs/{runId}/boarding-change-requests
    @GetMapping("/api/runs/{runId}/boarding-change-requests")
    public List<BoardingChangeResponse> listByRun(@PathVariable UUID runId) {
        return service.listByRun(runId).stream().map(BoardingChangeResponse::from).toList();
    }
}

