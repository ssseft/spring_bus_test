package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.DrivingRepository;
import com.example.bustest.Repository.bus.DrivingStudentRepository;
import com.example.bustest.Repository.bus.ScheduleRepository;
import com.example.bustest.Repository.bus.RouteRepository;
import com.example.bustest.domain.bus.Route;
import com.example.bustest.domain.bus.Driving;
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
public class DrivingService {

    private final ScheduleRepository scheduleRepository;
    private final DrivingRepository drivingRepository;
    private final RouteRepository routeRepository;
    private final DrivingStudentRepository drivingStudentRepository;

    @Transactional
    public void upsertRange(UUID scheduleId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) return;
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        LocalDate d = from;
        while (!d.isAfter(to)) {
            if (schedule.getIsActive() && dayMatches(d.getDayOfWeek(), schedule.getRepeatDays())) {
                Optional<Driving> existing = drivingRepository.findByScheduleAndDate(schedule, d);
                if (existing.isEmpty()) {
                    Driving driving = Driving.builder()
                            .schedule(schedule)
                            .date(d)
                            .status(Driving.drivingStatus.scheduled)
                            .route(null)
                            .build();
                    drivingRepository.save(driving);
                }
            }
            d = d.plusDays(1);
        }
    }

    @Transactional
    public Driving setStatus(UUID scheduleId, LocalDate date, boolean canceled) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        Driving driving = drivingRepository.findByScheduleAndDate(schedule, date)
                .orElse(null);
        if (driving == null) {
            driving = Driving.builder()
                    .schedule(schedule)
                    .date(date)
                    .status(Driving.drivingStatus.scheduled)
                    .route(null)
                    .build();
            drivingRepository.save(driving);
        }
        driving.update(canceled ? Driving.drivingStatus.canceled : Driving.drivingStatus.scheduled, driving.getRoute());
        return driving;
    }

    @Transactional
    public Driving overrideRoute(UUID scheduleId, LocalDate date, UUID routeId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));
        Driving driving = drivingRepository.findByScheduleAndDate(schedule, date)
                .orElse(null);
        if (driving == null) {
            driving = Driving.builder()
                    .schedule(schedule)
                    .date(date)
                    .status(Driving.drivingStatus.scheduled)
                    .route(null)
                    .build();
            drivingRepository.save(driving);
        }
        Route route = null;
        if (routeId != null) {
            route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        }
        driving.update(driving.getStatus(), route);
        return driving;
    }

    public List<Driving> list(UUID scheduleId, LocalDate from, LocalDate to) {
        return drivingRepository.findByScheduleIdAndDateBetween(scheduleId, from, to);
    }

    public Driving get(UUID drivingId) {
        return drivingRepository.findById(drivingId)
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
        Driving driving = drivingRepository.findByScheduleAndDate(schedule, date)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        int cnt = drivingStudentRepository.countByDriving_Id(driving.getId());
        if (cnt == 0) {
            drivingRepository.delete(driving);
            return true;
        } else {
            driving.update(Driving.drivingStatus.canceled, driving.getRoute());
            return false;
        }
    }
}
