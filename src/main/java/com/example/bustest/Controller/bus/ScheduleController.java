package com.example.bustest.Controller.bus;

import com.example.bustest.Service.bus.ScheduleService;
import com.example.bustest.Repository.bus.ScheduleStudentRepository;
import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.domain.bus.Schedule;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import com.example.bustest.domain.bus.ScheduleStudent;
import com.example.bustest.dto.schedule.ScheduleCreateRequest;
import com.example.bustest.dto.schedule.ScheduleResponse;
import com.example.bustest.dto.schedule.ScheduleStudentRequest;
import com.example.bustest.dto.schedule.ScheduleUpdateRequest;
import com.example.bustest.dto.schedule.ScheduleRouteChangeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
//ScheduleStudent관련 service도 여기서 해결 (구분하기가 애매한 부분이 있어서 이곳에 전부 넣음)
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleStudentRepository scheduleStudentRepository;

    @PostMapping
    public ScheduleResponse create(@RequestBody ScheduleCreateRequest req) {
        if (req.getAssignments() == null || req.getAssignments().isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "assignments is required and must not be empty");
        }
        Schedule s = scheduleService.createWithStudents(
                req.getAcademyId(),
                req.getRouteId(),
                req.getName(),
                req.getRepeatDays(),
                req.getStartTime(),
                req.getBoardingStatus(),
                req.getAssignments()
        );
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());
        return ScheduleResponse.from(s, assigns);
    }


    @PatchMapping("/{scheduleId}")
    public ScheduleResponse update(@PathVariable UUID scheduleId, @RequestBody ScheduleUpdateRequest req) {
        Schedule s = scheduleService.update(
                scheduleId,
                req.getRouteId(),
                req.getName(),
                req.getRepeatDays(),
                req.getStartTime(),
                req.getEndTime(),
                req.getBoardingStatus(),
                req.getIsActive()
        );
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());
        return ScheduleResponse.from(s, assigns);
    }


    // 타요/안타요 토글 관련 activate/ deactivate
    @PostMapping("/{scheduleId}:activate")
    public ResponseEntity<?> activate(@PathVariable UUID scheduleId) {
        scheduleService.activate(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{scheduleId}:deactivate")
    public ResponseEntity<?> deactivate(@PathVariable UUID scheduleId) {
        scheduleService.deactivate(scheduleId);
        return ResponseEntity.noContent().build();
    }


    //스케줄 단건 조회
    @GetMapping("/{scheduleId}")
    public ScheduleResponse get(@PathVariable UUID scheduleId) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());
        return ScheduleResponse.from(s, assigns);
    }
    
    //스케줄 list 조회
    @GetMapping
    public List<ScheduleResponse> list(@RequestParam(required = false) UUID academyId) {
        List<Schedule> items = (academyId != null)
                ? scheduleRepository.findByAcademyId(academyId)
                : scheduleRepository.findAll();
        return items.stream()
                .map(s -> ScheduleResponse.from(s, scheduleStudentRepository.findByScheduleId(s.getId())))
                .toList();
    }

    //스케줄 삭제
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> delete(@PathVariable UUID scheduleId) {
        scheduleService.delete(scheduleId);
        return ResponseEntity.noContent().build();
    }

    //학생- 정류장 매핑 추가
    @PostMapping("/{scheduleId}/assignments")
    public ScheduleResponse addAssignments(@PathVariable UUID scheduleId,
                                           @RequestBody List<@Valid ScheduleStudentRequest> items) {
        Schedule s = scheduleService.addAssignments(scheduleId, items);
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());
        return ScheduleResponse.from(s, assigns);
    }

    //학생- 정류장 매핑 테이블 수정
    @PatchMapping("/{scheduleId}/assignments/{studentId}")
    public ScheduleResponse updateAssignment(@PathVariable UUID scheduleId,
                                             @PathVariable UUID studentId,
                                             @RequestBody @Valid ScheduleStudentRequest body) {
        Schedule s = scheduleService.updateAssignment(scheduleId, studentId, body.getBusStopId());
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());
        return ScheduleResponse.from(s, assigns);
    }

    //학생-정류장 매핑 테이블 삭제
    @DeleteMapping("/{scheduleId}/assignments/{studentId}")
    public ResponseEntity<?> deleteAssignment(@PathVariable UUID scheduleId,
                                              @PathVariable UUID studentId) {
        scheduleService.deleteAssignment(scheduleId, studentId);
        return ResponseEntity.noContent().build();
    }


    
    //노선 변경(스케줄에서) -> 이거는 스케줄에 포함된 학생들 전부 삭제
    @PostMapping("/{scheduleId}:change-route")
    public ScheduleResponse changeRoute(@PathVariable UUID scheduleId,
                                        @RequestBody ScheduleRouteChangeRequest req) {
        UUID newRouteId;
        //수정 요청이 없을 경우에는 기존 노선으로
        if(req == null) newRouteId = null;
        else newRouteId = req.getRouteId();
        Schedule s = scheduleService.changeRouteAndResetAssignments(scheduleId, newRouteId);
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());

        return ScheduleResponse.from(s, assigns);
    }
}
