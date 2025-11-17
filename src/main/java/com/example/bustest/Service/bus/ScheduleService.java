package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.Repository.bus.RouteRepository;
import com.example.bustest.Repository.bus.ScheduleStudentRepository;
import com.example.bustest.Repository.bus.RouteStopRepository;
import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.Repository.user.StudentRepository;
import com.example.bustest.domain.bus.Route;
import com.example.bustest.domain.bus.Schedule;
import com.example.bustest.domain.bus.ScheduleStudent;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.domain.user.Student;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import com.example.bustest.dto.schedule.ScheduleStudentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;
    private final RunService dailyPlanService;
    private final ScheduleStudentRepository scheduleStudentRepository;
    private final RouteStopRepository routeStopRepository;
    private final StudentRepository studentRepository;
    private final BusStopRepository busStopRepository;

    @Transactional
    public Schedule create(UUID academyId,
                           UUID routeId,
                           String name,
                           Integer repeatDays,
                           LocalTime startTime,
                           Schedule.BoardingStatus boardingStatus,
                           Boolean isActive) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        LocalTime total = route.getTotalTime();

        //endtime은 starttime+totalitme으로 계산
        //getHour,Minute,Second는 LocalTime함수로, plusHours를 통해 계산
        //LocalTime함수 관련 참고 사이트 https://covenant.tistory.com/255
        LocalTime computedEnd = (startTime
                .plusHours(total.getHour())
                .plusMinutes(total.getMinute())
                .plusSeconds(total.getSecond()));

        Schedule s = Schedule.builder()
                .academyId(academyId)
                .route(route)
                .name(name)
                .repeatDays(repeatDays)
                .startTime(startTime)
                .endTime(computedEnd)
                .boardingStatus(boardingStatus)
                .isActive(isActive != null ? isActive : true) //기본값 true 설정
                .build();
        return scheduleRepository.save(s);
    }

    @Transactional
    public Schedule createWithStudents(UUID academyId,
                                       UUID routeId,
                                       String name,
                                       Integer repeatDays,
                                       LocalTime startTime,
                                       Schedule.BoardingStatus boardingStatus,
                                       java.util.List<ScheduleStudentRequest> assignments) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        // endtime은 시작시간 + route테이블의 total_time으로 계산
        LocalTime total = route.getTotalTime();
        LocalTime endTime = startTime
                .plusHours(total.getHour())
                .plusMinutes(total.getMinute())
                .plusSeconds(total.getSecond());

        Schedule s = Schedule.builder()
                .academyId(academyId)
                .route(route)
                .name(name)
                .repeatDays(repeatDays)
                .startTime(startTime)
                .endTime(endTime)
                .boardingStatus(boardingStatus)
                .isActive(true)
                .build();
        scheduleRepository.save(s);

        // assignments 필수체크
        // 학생이 없으면 schedule생성 불가임
        if (assignments == null || assignments.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "assignments is required and must not be empty");
        }
        Set<UUID> Students = new HashSet<>();
        //학생들을 hashset에 저장 처음엔 list로 했는데 list.contain 속도가 너무 걸릴거같아 hashset으로
        for (ScheduleStudentRequest c : assignments) {
            //아마 academy_id로 정류장,학생 조회 -> 해당 정류장,학생 매핑할거라 null값이 들어올 일은 없을거 같긴한데 일단 넣음
            if (c.getStudentId() == null || c.getBusStopId() == null) {
                throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "assignment requires both studentId and busStopId");
            }
            //중복학생 체크 이건 필요한듯
            if (!Students.add(c.getStudentId())) {
                throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "duplicate student in assignments");
            }

            // 검증 -> 학생/정류장 존재, 해당 노선 정류장인지 확인
            Student student = studentRepository.findById(c.getStudentId())
                    .orElseThrow(() -> new BaseException(ErrorCode.STUDENT_NOT_FOUND));
            BusStop stop = busStopRepository.findById(c.getBusStopId())
                    .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));

            // 정류장이 해당 노선에 속하는지 체크하는 코드
            if (!routeStopRepository.existsByRoute_IdAndBusStop_Id(route.getId(), stop.getId())) {
                throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
            }


            //매핑하는거
            ScheduleStudent mapping = ScheduleStudent.builder()
                    .schedule(s)
                    .student(student)
                    .busStop(stop)
                    .build();

            scheduleStudentRepository.save(mapping);
        }
        return s;
    }

    @Transactional
    public Schedule update(UUID scheduleId,
                           UUID routeId,
                           String name,
                           Integer repeatDays,
                           LocalTime startTime,
                           LocalTime endTime,
                           Schedule.BoardingStatus boardingStatus,
                           Boolean isActive) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        Route route = null; // routeid 변경 없으면 null로

        if (routeId != null) {
            //변경이 있으면 route_id변경
            route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND)); //존재 검증
        }
        s.update(route, name, repeatDays, startTime, endTime, boardingStatus, isActive);
        return s;
    }


    // 아래 activate/deactivate는 그냥 isActive만 바꾸는 메소드로 분리
    // 토글을 위해 분리해논건데 나중에 update에 합칠수도..? 아마 안 할거 같긴 함
    @Transactional
    public void activate(UUID scheduleId) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        s.update(null, null, null, null, null, null, true);
    }

    @Transactional
    public void deactivate(UUID scheduleId) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        s.update(null, null, null, null, null, null, false);
    }

    // 기간조정을 해 run테이블 생성 함수 아직 미구현
    @Transactional
    public void buildPlans(UUID scheduleId, LocalDate from, LocalDate to) {
        dailyPlanService.upsertRange(scheduleId, from, to);
    }
    
    //스케줄 삭제
    // schedule_students 먼저 삭제 후 스케줄 삭제
    @Transactional
    public void delete(UUID scheduleId) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(scheduleId);
        if (!assigns.isEmpty()) {
            scheduleStudentRepository.deleteAll(assigns);
        }
        scheduleRepository.delete(s);
    }

    // 배정(학생-정류장) 추가
    @Transactional
    public Schedule addAssignments(UUID scheduleId, List<ScheduleStudentRequest> items) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (items == null || items.isEmpty())
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "assignments is required");

        Set<UUID> seen = new HashSet<>();
        for (ScheduleStudentRequest it : items) {
            if (it.getStudentId() == null || it.getBusStopId() == null)
                throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "studentId and busStopId required");
            if (!seen.add(it.getStudentId()))
                throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "duplicate student in request");
        }

        List<UUID> studentIds = items.stream().map(ScheduleStudentRequest::getStudentId).collect(Collectors.toList());
        List<UUID> stopIds = items.stream().map(ScheduleStudentRequest::getBusStopId).collect(Collectors.toList());
        List<Student> students = studentRepository.findAllById(studentIds);
        List<BusStop> stops = busStopRepository.findAllById(stopIds);

        //route랑 busstop 매칭 확인코드
        UUID routeId = s.getRoute().getId();
        for (ScheduleStudentRequest it : items) {
            if (!routeStopRepository.existsByRoute_IdAndBusStop_Id(routeId, it.getBusStopId()))
                throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "busStop not in route");
            boolean exists = scheduleStudentRepository.findByScheduleIdAndStudentId(scheduleId, it.getStudentId()).isPresent();
            if (exists) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "student already assigned");
        }

        for (ScheduleStudentRequest it : items) {
            Student student = students.stream().filter(x -> x.getId().equals(it.getStudentId())).findFirst().orElse(null);
            BusStop stop = stops.stream().filter(x -> x.getId().equals(it.getBusStopId())).findFirst().orElse(null);
            if (student == null || stop == null) continue;
            ScheduleStudent mapping = ScheduleStudent.builder()
                    .schedule(s).student(student).busStop(stop).build();
            scheduleStudentRepository.save(mapping);
        }
        return s;
    }

    @Transactional
    public Schedule updateAssignment(UUID scheduleId, UUID studentId, UUID newBusStopId) {
        if (newBusStopId == null)
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "busStopId required");

        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        ScheduleStudent mapping = scheduleStudentRepository.findByScheduleIdAndStudentId(scheduleId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE, "assignment not found"));

        UUID routeId = s.getRoute().getId();
        if (!routeStopRepository.existsByRoute_IdAndBusStop_Id(routeId, newBusStopId))
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "busStop not in route");
        BusStop stop = busStopRepository.findById(newBusStopId)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
        mapping.update(stop);
        return s;
    }

    @Transactional
    public void deleteAssignment(UUID scheduleId, UUID studentId) {
        ScheduleStudent mapping = scheduleStudentRepository.findByScheduleIdAndStudentId(scheduleId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE, "assignment not found"));
        scheduleStudentRepository.delete(mapping);
    }

    @Transactional
    public Schedule changeRouteAndResetAssignments(UUID scheduleId, UUID newRouteId) {
        if (newRouteId == null) throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "routeId required");
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        Route route = routeRepository.findById(newRouteId)
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));

        // 노선변경하면 학생 전부 초기화 하는 코드
        List<ScheduleStudent> assigns = scheduleStudentRepository.findByScheduleId(scheduleId);
        if (!assigns.isEmpty()) scheduleStudentRepository.deleteAll(assigns);
        LocalTime total = route.getTotalTime();
        LocalTime newEnd = s.getStartTime()
                .plusHours(total.getHour())
                .plusMinutes(total.getMinute())
                .plusSeconds(total.getSecond());
        s.update(route, null, null, null, newEnd, null, null);
        return s;
    }
}
