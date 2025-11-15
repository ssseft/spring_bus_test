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
import com.example.bustest.dto.schedule.ScheduleWithStudentsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleStudentRepository scheduleStudentRepository;

    @PostMapping
    public ScheduleResponse create(@RequestBody ScheduleCreateRequest req) {
        Schedule s;
        if (req.getAssignments() != null && !req.getAssignments().isEmpty()) {
            // 학생 배정까지 함께 생성
            s = scheduleService.createWithStudents(
                    req.getAcademyId(),
                    req.getRouteId(),
                    req.getName(),
                    req.getRepeatDays(),
                    req.getStartTime(),
                    req.getBoardingStatus(),
                    req.getAssignments()
            );
        } else {
            // 스케줄만 생성
            s = scheduleService.create(
                    req.getAcademyId(),
                    req.getRouteId(),
                    req.getName(),
                    req.getRepeatDays(),
                    req.getStartTime(),
                    req.getEndTime(),
                    req.getBoardingStatus(),
                    req.getIsActive()
            );
        }
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());
        return ScheduleResponse.from(s, assigns);
    }

    @PostMapping(":with-students")
    public ScheduleResponse createWithStudents(@RequestBody ScheduleWithStudentsRequest req) {
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

    @GetMapping("/{scheduleId}")
    public ScheduleResponse get(@PathVariable UUID scheduleId) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(s.getId());
        return ScheduleResponse.from(s, assigns);
    }

    @GetMapping
    public List<ScheduleResponse> list(@RequestParam(required = false) UUID academyId) {
        List<Schedule> items = (academyId != null)
                ? scheduleRepository.findByAcademyId(academyId)
                : scheduleRepository.findAll();
        return items.stream()
                .map(s -> ScheduleResponse.from(s, scheduleStudentRepository.findByScheduleId(s.getId())))
                .toList();
    }
}
