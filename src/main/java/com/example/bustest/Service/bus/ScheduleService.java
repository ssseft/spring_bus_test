package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.Repository.map.RouteRepository;
import com.example.bustest.Repository.bus.ScheduleStudentRepository;
import com.example.bustest.Repository.bus.RouteStopRepository;
import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.Repository.user.StudentRepository;
import com.example.bustest.Service.bus.ScheduleDailyPlanService;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;
    private final ScheduleDailyPlanService dailyPlanService;
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
                           LocalTime endTime,
                           Schedule.BoardingStatus boardingStatus,
                           Boolean isActive) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        Schedule s = Schedule.builder()
                .academyId(academyId)
                .route(route)
                .name(name)
                .repeatDays(repeatDays)
                .startTime(startTime)
                .endTime(endTime)
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
        // 종료시각 계산: 시작시간 + 노선 total_time
        LocalTime total = route.getTotalTime();
        //이런 함수가 있더라구요 궁금한 사람은 (https://sunghs.tistory.com/128) 참고
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

        if (assignments != null) {
            for (ScheduleStudentRequest c : assignments) {
                //에러 검증 학생,정류장, 노선에 포함된 정류장인지
                Student student = studentRepository.findById(c.getStudentId())
                        .orElseThrow(() -> new BaseException(ErrorCode.STUDENT_NOT_FOUND));
                BusStop stop = busStopRepository.findById(c.getBusStopId())
                        .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
                if (!routeStopRepository.existsByRoute_IdAndBusStop_Id(route.getId(), stop.getId())) {
                    throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
                }


                ScheduleStudent mapping = ScheduleStudent.builder()
                        .schedule(s)
                        .student(student)
                        .busStop(stop)
                        .build();
                scheduleStudentRepository.save(mapping);
            }
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
        Route route = null; // routeid변경이 없으면 이전값 유지,
        if (routeId != null) {
            route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND)); //혹시 모르는 예외처리
        }
        s.update(route, name, repeatDays, startTime, endTime, boardingStatus, isActive);
        return s;
    }


    //아래 activate/deactivate는 그냥 is_Active만 바꾸는 메소드 하나로 합쳐도 됨 (isActive = !isActive 이런 식으로)
    //기능 분리가 좋다길래 일단 분리해 놓았음
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

    //일정 기간동안 스케쥴 생성 이건 고민중
    //생각하고 있는 시나리오는 8월 생성 -> 리스트 만들어짐 -> 삭제 선택(휴무일 등) ->
    // 생성 버튼 누르면 ScheduleDailyPlan 테이블 생성
    @Transactional
    public void rebuildPlans(UUID scheduleId, LocalDate from, LocalDate to) {
        dailyPlanService.upsertRange(scheduleId, from, to);
    }
}
