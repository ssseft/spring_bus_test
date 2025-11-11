package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.Repository.map.RouteRepository;
import com.example.bustest.Service.bus.ScheduleDailyPlanService;
import com.example.bustest.domain.bus.Route;
import com.example.bustest.domain.bus.Schedule;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
        Route route = null; //라우트 변경 안함을 나타내는 센티넬 routeid변경이 없으면 이전값 유지,
        if (routeId != null) {
            route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND)); //혹시 모르는 예외처리(필요없으면 지워도됨)
        }
        s.update(route, name, repeatDays, startTime, endTime, boardingStatus, isActive);
        return s;
    }


    //아래 activate/deactivate는 그냥 is_Active만 바꾸는거라 도메인 내에서 해도 되지만나중에 run부분에 필요할 수도 있으니 여기에 남겨두기
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

    //일정 기간동안 스케쥴 생성
    @Transactional
    public void rebuildPlans(UUID scheduleId, LocalDate from, LocalDate to) {
        dailyPlanService.upsertRange(scheduleId, from, to);
    }
}
