package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.RunRepository;
import com.example.bustest.Repository.bus.RunStudentRepository;
import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.Repository.map.RouteRepository;
import com.example.bustest.domain.bus.Route;
import com.example.bustest.domain.bus.Run;
import com.example.bustest.domain.bus.Schedule;
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
public class RunService {

    private final ScheduleRepository scheduleRepository;
    private final RunRepository runRepository;
    private final RouteRepository routeRepository;
    private final RunStudentRepository runStudentRepository;

    @Transactional
    public void upsertRange(UUID scheduleId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) return;
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        LocalDate d = from;
        while (!d.isAfter(to)) {
            if (schedule.getIsActive() && dayMatches(d.getDayOfWeek(), schedule.getRepeatDays())) {
                Optional<Run> existing = runRepository.findByScheduleAndDate(schedule, d);
                if (existing.isEmpty()) {
                    Run run = Run.builder()
                            .schedule(schedule)
                            .date(d)
                            .status(Run.RunStatus.scheduled)
                            .route(null)
                            .build();
                    runRepository.save(run);
                }
            }
            d = d.plusDays(1);
        }
    }

    @Transactional
    public Run setStatus(UUID scheduleId, LocalDate date, boolean canceled) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        Run run = runRepository.findByScheduleAndDate(schedule, date)
                .orElse(null);
        if (run == null) {
            run = Run.builder()
                    .schedule(schedule)
                    .date(date)
                    .status(Run.RunStatus.scheduled)
                    .route(null)
                    .build();
            runRepository.save(run);
        }
        run.update(canceled ? Run.RunStatus.canceled : Run.RunStatus.scheduled, run.getRoute());
        return run;
    }

    @Transactional
    public Run overrideRoute(UUID scheduleId, LocalDate date, UUID routeId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        Run run = runRepository.findByScheduleAndDate(schedule, date)
                .orElse(null);
        if (run == null) {
            run = Run.builder()
                    .schedule(schedule)
                    .date(date)
                    .status(Run.RunStatus.scheduled)
                    .route(null)
                    .build();
            runRepository.save(run);
        }
        Route route = null;
        if (routeId != null) {
            route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        }
        run.update(run.getStatus(), route);
        return run;
    }

    public List<Run> list(UUID scheduleId, LocalDate from, LocalDate to) {
        return runRepository.findByScheduleIdAndDateBetween(scheduleId, from, to);
    }

    public Run get(UUID runId) {
        return runRepository.findById(runId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
    }

    private boolean dayMatches(DayOfWeek s, Integer mask) {
        int bit = 1 << (s.getValue() - 1);
        if ((mask & bit) != 0) return false; else return true;
    }

    @Transactional
    public boolean deleteOrCancel(UUID scheduleId, LocalDate date) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        Run run = runRepository.findByScheduleAndDate(schedule, date)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        int cnt = runStudentRepository.countByRunId(run.getId());
        if (cnt == 0) {
            runRepository.delete(run);
            return true;
        } else {
            run.update(Run.RunStatus.canceled, run.getRoute());
            return false;
        }
    }
}
