package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.ScheduleDailyPlanRepository;
import com.example.bustest.Repository.bus.ScheduleStudentRepository;
import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.Repository.user.StudentRepository;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.domain.bus.ScheduleDailyPlan;
import com.example.bustest.domain.bus.ScheduleStudent;
import com.example.bustest.domain.user.Student;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
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
public class ScheduleStudentService {

    private final ScheduleStudentRepository scheduleStudentRepository;
    private final ScheduleDailyPlanRepository planRepository;
    private final StudentRepository studentRepository;
    private final BusStopRepository busStopRepository;

    @Transactional
    public ScheduleStudent reserve(UUID planId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        ScheduleDailyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (plan.getStatus() == ScheduleDailyPlan.Status.CANCELED) {
            throw new IllegalStateException("Plan is canceled (no service)");
        }
        scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .ifPresent(ss -> { throw new IllegalStateException("Already reserved for this plan"); });

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDENT_NOT_FOUND));
        BusStop busStop = busStopRepository.findById(busStopId)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));

        ScheduleStudent ss = ScheduleStudent.builder()
                .scheduleDailyPlan(plan)
                .student(student)
                .date(plan.getDate())
                .busStop(busStop)
                .plannedTime(plannedTime)
                .scheduleStatus(ScheduleStudent.ScheduleStatus.RESERVED)
                .build();
        return scheduleStudentRepository.save(ss);
    }

    @Transactional
    public ScheduleStudent cancel(UUID planId, UUID studentId) {
        ScheduleStudent ss = scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .orElseThrow(() -> new IllegalStateException("Reservation not found"));
        ss.update(null, null, ScheduleStudent.ScheduleStatus.CANCELED);
        return ss;
    }

    @Transactional
    public ScheduleStudent updateStatus(UUID planId, UUID studentId, ScheduleStudent.ScheduleStatus status) {
        ScheduleStudent ss = scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .orElseThrow(() -> new IllegalStateException("Reservation not found"));
        ss.update(null, null, status);
        return ss;
    }

    @Transactional
    public ScheduleStudent updateAssignment(UUID planId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        ScheduleStudent ss = scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .orElseThrow(() -> new IllegalStateException("Reservation not found"));
        BusStop bs = null;
        if (busStopId != null) {
            bs = busStopRepository.findById(busStopId)
                    .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
        }
        ss.update(bs, plannedTime, null);
        return ss;
    }

    public List<ScheduleStudent> listByPlan(UUID planId) {
        return scheduleStudentRepository.findByScheduleDailyPlanId(planId);
    }

    public List<ScheduleStudent> listByStudent(UUID studentId, LocalDate from, LocalDate to) {
        return scheduleStudentRepository.findByStudentIdAndDateBetween(studentId, from, to);
    }
}
