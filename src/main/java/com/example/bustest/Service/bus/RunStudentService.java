package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.RunRepository;
import com.example.bustest.Repository.bus.RunStudentRepository;
import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.Repository.bus.RouteStopRepository;
import com.example.bustest.Repository.map.RouteRepository;
import com.example.bustest.Repository.user.StudentRepository;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.domain.bus.Run;
import com.example.bustest.domain.bus.RunStudent;
import com.example.bustest.domain.bus.RouteStop;
import com.example.bustest.domain.user.Student;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import com.example.bustest.Service.map.RoutePersistService;
import com.example.bustest.dto.map.RouteCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RunStudentService {

    private final RunStudentRepository runStudentRepository;
    private final RunRepository runRepository;
    private final StudentRepository studentRepository;
    private final BusStopRepository busStopRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final RoutePersistService routePersistService;

    @Transactional
    public RunStudent reserve(UUID runId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (run.getStatus() == Run.RunStatus.canceled) {
            throw new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_CANCELED);
        }
        runStudentRepository.findByRunIdAndStudentId(runId, studentId)
                .ifPresent(ss -> { throw new BaseException(ErrorCode.SCHEDULE_STUDENT_ALREADY_RESERVED); });

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDENT_NOT_FOUND));
        BusStop busStop = busStopRepository.findById(busStopId)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));

        RunStudent ss = RunStudent.builder()
                .run(run)
                .student(student)
                .date(run.getDate())
                .busStop(busStop)
                .plannedTime(plannedTime)
                .status(RunStudent.RunStudentStatus.reserved)
                .build();
        return runStudentRepository.save(ss);
    }

    @Transactional
    public RunStudent cancel(UUID runId, UUID studentId) {
        RunStudent ss = runStudentRepository.findByRunIdAndStudentId(runId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        ss.update(null, null, RunStudent.RunStudentStatus.canceled);
        return ss;
    }

    @Transactional
    public RunStudent updateStatus(UUID runId, UUID studentId, RunStudent.RunStudentStatus status) {
        RunStudent ss = runStudentRepository.findByRunIdAndStudentId(runId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        ss.update(null, null, status);
        return ss;
    }

    @Transactional
    public RunStudent updateAssignment(UUID runId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        RunStudent ss = runStudentRepository.findByRunIdAndStudentId(runId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        BusStop bs = null;
        if (busStopId != null) {
            bs = busStopRepository.findById(busStopId)
                    .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
        }
        ss.update(bs, plannedTime, null);
        return ss;
    }

    public List<RunStudent> listByRun(UUID runId) {
        return runStudentRepository.findByRunId(runId);
    }

    public List<RunStudent> listByStudent(UUID studentId, LocalDate from, LocalDate to) {
        return runStudentRepository.findByStudentIdAndDateBetween(studentId, from, to);
    }

    /**
     * 취소 후 같은 정류장에 남은 승객이 없으면 임시 Route 재생성하여 Run에 반영.
     * - Run.status == scheduled 에서만 허용
     * - 남은 승객 체크는 reserved/boarded 포함으로 판단(정책에 맞게 조정 가능)
     */
    @Transactional
    public boolean cancelAndMaybeRebuildRoute(UUID runId, UUID studentId) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (run.getStatus() != Run.RunStatus.scheduled) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }

        RunStudent ss = runStudentRepository.findByRunIdAndStudentId(runId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));

        // already canceled → no-op
        if (ss.getStatus() == RunStudent.RunStudentStatus.canceled) return false;

        // 1) cancel
        ss.update(null, null, RunStudent.RunStudentStatus.canceled);

        // 2) check remaining at the same stop
        UUID busStopId = ss.getBusStop().getId();
        List<RunStudent.RunStudentStatus> active = List.of(RunStudent.RunStudentStatus.reserved, RunStudent.RunStudentStatus.boarded);
        boolean othersExist = runStudentRepository.existsByRunIdAndBusStopIdAndStatusInAndStudentIdNot(runId, busStopId, active, studentId);
        if (othersExist) return false;

        // 3) rebuild temporary route by removing the stop
        UUID currentRouteId = run.getRoute() != null ? run.getRoute().getId() : run.getSchedule().getRoute().getId();
        List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrder(currentRouteId);
        // remove the target stop
        List<UUID> newOrdered = stops.stream()
                .map(rs -> rs.getBusStop().getId())
                .filter(id -> !id.equals(busStopId))
                .toList();
        if (newOrdered.size() < 2) {
            // not enough to rebuild a valid route; keep canceled only
            return false;
        }

        // Build temporary route via RoutePersistService
        RouteCreateRequest req = new RouteCreateRequest();
        req.setName("TEMP-RUN-" + run.getId());
        req.setOrderedBusStopIds(newOrdered);
        UUID academyId = run.getSchedule().getAcademyId();
        var resp = routePersistService.create(academyId, req);

        // attach new route to run
        var newRoute = routeRepository.findById(resp.getRouteId())
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        run.update(null, newRoute); // keep status as-is, just swap route

        // remap plannedTime for remaining (non-canceled) run students
        for (RunStudent other : runStudentRepository.findByRunId(runId)) {
            if (other.getStatus() == RunStudent.RunStudentStatus.canceled) continue;
            var rsOpt = routeStopRepository.findByRoute_IdAndBusStop_Id(newRoute.getId(), other.getBusStop().getId());
            rsOpt.ifPresent(rs -> other.update(null, rs.getStartToArriveTime(), null));
        }

        return true;
    }
}
