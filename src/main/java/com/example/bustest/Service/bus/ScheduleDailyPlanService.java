package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.ScheduleDailyPlanRepository;
import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.Repository.map.RouteRepository;
import com.example.bustest.Repository.bus.ScheduleDailyPlanStudentRepository;
import com.example.bustest.domain.bus.Route;
import com.example.bustest.domain.bus.Schedule;
import com.example.bustest.domain.bus.ScheduleDailyPlan;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleDailyPlanService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleDailyPlanRepository planRepository;
    private final RouteRepository routeRepository;
    private final ScheduleDailyPlanStudentRepository scheduleStudentRepository;

    @Transactional
    public void upsertRange(UUID scheduleId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) return;
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        LocalDate d = from;
        while (!d.isAfter(to)) {
            //getDayOfWeek는 요일 반환 메소드(MONDAY,SUNDAY 이렇게 반환함)
            if (schedule.getIsActive()&& dayMatches(d.getDayOfWeek(),schedule.getRepeatDays())) {
                Optional<ScheduleDailyPlan> existing = planRepository.findByScheduleAndDate(schedule, d);
                if (existing.isEmpty()) {
                    ScheduleDailyPlan plan = ScheduleDailyPlan.builder()
                            .schedule(schedule)
                            .date(d)
                            .Status(ScheduleDailyPlan.Status.OPER)
                            .route(null)
                            .build();
                    planRepository.save(plan);
                }
            }
            d = d.plusDays(1); // 이거 당연할 수도 있는데 d++하면 안됨 이런식으로 하루 늘려야함
        }
    }

    @Transactional
    public ScheduleDailyPlan setNoService(UUID scheduleId, LocalDate date, boolean canceled) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        ScheduleDailyPlan plan = planRepository.findByScheduleAndDate(schedule, date)
                .orElse(null);
        //plan이 존재하면 기존거 사용 없는경우 null
        if (plan == null) { //여기서 plan.equals(null)을 쓰면 항상 false가 됨 ==null로 비교
            plan = ScheduleDailyPlan.builder()
                    .schedule(schedule)
                    .date(date)
                    .Status(ScheduleDailyPlan.Status.OPER)
                    .route(null)
                    .build();
            planRepository.save(plan);
        }
        if(canceled){
            plan.update(ScheduleDailyPlan.Status.CANCELED,plan.getRoute());
        }
        else{
            plan.update(ScheduleDailyPlan.Status.OPER, plan.getRoute());
        }
        return plan;
    }

    @Transactional
    public ScheduleDailyPlan overrideRoute(UUID scheduleId, LocalDate date, UUID routeId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        ScheduleDailyPlan plan = planRepository.findByScheduleAndDate(schedule, date)
                .orElse(null);
        //plan이 존재하면 기존거 사용 없는경우 null
        if (plan == null) {
            plan = ScheduleDailyPlan.builder()
                    .schedule(schedule)
                    .date(date)
                    .Status(ScheduleDailyPlan.Status.OPER)
                    .route(null)
                    .build();
            planRepository.save(plan);
        }
        Route route = null;
        if (routeId != null) {
            route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        }
        plan.update(plan.getStatus(), route);
        return plan;
    }

    public List<ScheduleDailyPlan> list(UUID scheduleId, LocalDate from, LocalDate to) {
        return planRepository.findByScheduleIdAndDateBetween(scheduleId, from, to);
    }

    public ScheduleDailyPlan get(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
    }
    
    //DayOfWeek 라고 관련 클래스가 있어서 이걸로 사용
    //getValue하면 MONDAY = 1, TUESDAY =2 이런식으로 반환
    private boolean dayMatches(DayOfWeek s, Integer mask) {
        int bit = 1 << (s.getValue() - 1);
        if((mask&bit)!=0) return false;
        else  return true;
    }

    @Transactional
    public boolean deleteOrCancel(UUID scheduleId, LocalDate date) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        ScheduleDailyPlan plan = planRepository.findByScheduleAndDate(schedule, date)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        int cnt = scheduleStudentRepository.countByScheduleDailyPlanId(plan.getId());
        if (cnt==0) {
            planRepository.delete(plan);
            return true;
        } else {
            plan.update(ScheduleDailyPlan.Status.CANCELED, plan.getRoute());
            return false;
        }
    }
}