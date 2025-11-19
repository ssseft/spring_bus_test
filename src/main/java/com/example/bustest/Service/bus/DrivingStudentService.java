package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.DrivingRepository;
import com.example.bustest.Repository.bus.DrivingStudentRepository;
import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.Repository.bus.RouteStopRepository;
import com.example.bustest.Repository.bus.RouteRepository;
import com.example.bustest.Repository.user.StudentRepository;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.domain.bus.Driving;
import com.example.bustest.domain.bus.DrivingStudent;
import com.example.bustest.domain.bus.RouteStop;
import com.example.bustest.domain.user.Student;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
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
public class DrivingStudentService {

    private final DrivingStudentRepository drivingStudentRepository;
    private final DrivingRepository drivingRepository;
    private final StudentRepository studentRepository;
    private final BusStopRepository busStopRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final RoutePersistService routePersistService;

    @Transactional
    public DrivingStudent reserve(UUID drivingId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        Driving driving = drivingRepository.findById(drivingId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (driving.getStatus() == Driving.drivingStatus.canceled) {
            throw new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_CANCELED);
        }
        drivingStudentRepository.findByDriving_IdAndStudent_Id(drivingId, studentId)
                .ifPresent(ss -> { throw new BaseException(ErrorCode.SCHEDULE_STUDENT_ALREADY_RESERVED); });

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDENT_NOT_FOUND));
        BusStop busStop = busStopRepository.findById(busStopId)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));

        DrivingStudent ss = DrivingStudent.builder()
                .driving(driving)
                .student(student)
                .date(driving.getDate())
                .busStop(busStop)
                .plannedTime(plannedTime)
                .status(DrivingStudent.drivingStudentStatus.reserved)
                .build();
        return drivingStudentRepository.save(ss);
    }

    @Transactional
    public DrivingStudent cancel(UUID drivingId, UUID studentId) {
        DrivingStudent ss = drivingStudentRepository.findByDriving_IdAndStudent_Id(drivingId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        ss.update(null, null, DrivingStudent.drivingStudentStatus.canceled);
        return ss;
    }

    @Transactional
    public DrivingStudent updateStatus(UUID drivingId, UUID studentId, DrivingStudent.drivingStudentStatus status) {
        DrivingStudent ss = drivingStudentRepository.findByDriving_IdAndStudent_Id(drivingId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        ss.update(null, null, status);
        return ss;
    }

    @Transactional
    public DrivingStudent updateAssignment(UUID drivingId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        DrivingStudent ss = drivingStudentRepository.findByDriving_IdAndStudent_Id(drivingId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        BusStop bs = null;
        if (busStopId != null) {
            bs = busStopRepository.findById(busStopId)
                    .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
        }
        ss.update(bs, plannedTime, null);
        return ss;
    }

    public List<DrivingStudent> listByDriving(UUID drivingId) {
        return drivingStudentRepository.findByDriving_Id(drivingId);
    }

    public List<DrivingStudent> listByStudent(UUID studentId, LocalDate from, LocalDate to) {
        return drivingStudentRepository.findByStudentIdAndDateBetween(studentId, from, to);
    }

    /**
     * 취소 후 같은 정류장에 남은 승객이 없으면 임시 Route 재생성하여 Driving에 반영.
     * - Driving.status == scheduled 에서만 허용
     * - 남은 승객 체크는 reserved/boarded 포함으로 판단(정책에 맞게 조정 가능)
     */
    @Transactional
    public boolean cancelAndMaybeRebuildRoute(UUID drivingId, UUID studentId) {
        Driving driving = drivingRepository.findById(drivingId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (driving.getStatus() != Driving.drivingStatus.scheduled) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }

        DrivingStudent ss = drivingStudentRepository.findByDriving_IdAndStudent_Id(drivingId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));

        // already canceled → no-op
        if (ss.getStatus() == DrivingStudent.drivingStudentStatus.canceled) return false;

        // 1) cancel
        ss.update(null, null, DrivingStudent.drivingStudentStatus.canceled);

        // 2) check remaining at the same stop
        UUID busStopId = ss.getBusStop().getId();
        List<DrivingStudent.drivingStudentStatus> active = List.of(DrivingStudent.drivingStudentStatus.reserved, DrivingStudent.drivingStudentStatus.boarded);
        boolean othersExist = drivingStudentRepository.existsByDriving_IdAndBusStopIdAndStatusInAndStudentIdNot(drivingId, busStopId, active, studentId);
        if (othersExist) return false;

        // 3) rebuild temporary route by removing the stop
        UUID currentRouteId = driving.getRoute() != null ? driving.getRoute().getId() : driving.getSchedule().getRoute().getId();
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
        req.setName("TEMP-DRIVING-" + driving.getId());
        req.setOrderedBusStopIds(newOrdered);
        UUID academyId = driving.getSchedule().getAcademyId();
        var resp = routePersistService.create(academyId, req);

        // attach new route to driving
        var newRoute = routeRepository.findById(resp.getRouteId())
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        driving.update(null, newRoute); // keep status as-is, just swap route

        // remap plannedTime for remaining (non-canceled) driving students
        for (DrivingStudent other : drivingStudentRepository.findByDriving_Id(drivingId)) {
            if (other.getStatus() == DrivingStudent.drivingStudentStatus.canceled) continue;
            var rsOpt = routeStopRepository.findByRoute_IdAndBusStop_Id(newRoute.getId(), other.getBusStop().getId());
            rsOpt.ifPresent(rs -> other.update(null, rs.getStartToArriveTime(), null));
        }

        return true;
    }
}
