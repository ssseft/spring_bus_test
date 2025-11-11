package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.ScheduleDailyPlanRepository;
import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.Repository.map.RouteRepository;
import com.example.bustest.Repository.bus.ScheduleStudentRepository;
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
    private final ScheduleStudentRepository scheduleStudentRepository;

    @Transactional
    public void upsertRange(UUID scheduleId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) return;
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        LocalDate d = from;
        while (!d.isAfter(to)) {
            if (schedule.getIsActive() == Boolean.TRUE && dayMatches(d.getDayOfWeek(), schedule.getRepeatDays())) {
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
            d = d.plusDays(1);
        }
    }

    @Transactional
    public ScheduleDailyPlan setNoService(UUID scheduleId, LocalDate date, boolean canceled) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        ScheduleDailyPlan plan = planRepository.findByScheduleAndDate(schedule, date)
                .orElseGet(() -> planRepository.save(ScheduleDailyPlan.builder()
                        .schedule(schedule)
                        .date(date)
                        .Status(ScheduleDailyPlan.Status.OPER)
                        .route(null)
                        .build()));
        plan.update(canceled ? ScheduleDailyPlan.Status.CANCELED : ScheduleDailyPlan.Status.OPER, plan.getRoute());
        return plan;
    }

    @Transactional
    public ScheduleDailyPlan overrideRoute(UUID scheduleId, LocalDate date, UUID routeIdNullable) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        ScheduleDailyPlan plan = planRepository.findByScheduleAndDate(schedule, date)
                .orElseGet(() -> planRepository.save(ScheduleDailyPlan.builder()
                        .schedule(schedule)
                        .date(date)
                        .Status(ScheduleDailyPlan.Status.OPER)
                        .route(null)
                        .build()));
        Route route = null;
        if (routeIdNullable != null) {
            route = routeRepository.findById(routeIdNullable)
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

    private boolean dayMatches(DayOfWeek dow, Integer mask) {
        if (mask == null) return false;
        // 월=1 ... 일=7 기준으로 1<< (dow.getValue()-1)
        int bit = 1 << (dow.getValue() - 1);
        return (mask & bit) != 0;
    }

    @Transactional
    public boolean deleteOrCancel(UUID scheduleId, LocalDate date) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        ScheduleDailyPlan plan = planRepository.findByScheduleAndDate(schedule, date)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        long cnt = scheduleStudentRepository.countByScheduleDailyPlanId(plan.getId());
        if (cnt > 0) {
            plan.update(ScheduleDailyPlan.Status.CANCELED, plan.getRoute());
            return false; // canceled instead of delete
        } else {
            planRepository.delete(plan);
            return true;
        }
    }
}
