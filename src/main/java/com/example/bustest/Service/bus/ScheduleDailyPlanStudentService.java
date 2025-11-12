package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.ScheduleDailyPlanRepository;
import com.example.bustest.Repository.bus.ScheduleDailyPlanStudentRepository;
import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.Repository.user.StudentRepository;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.domain.bus.ScheduleDailyPlan;
import com.example.bustest.domain.bus.ScheduleDailyPlanStudent;
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
public class ScheduleDailyPlanStudentService {

    private final ScheduleDailyPlanStudentRepository scheduleStudentRepository;
    private final ScheduleDailyPlanRepository planRepository;
    private final StudentRepository studentRepository;
    private final BusStopRepository busStopRepository;

    @Transactional
    public ScheduleDailyPlanStudent reserve(UUID planId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        ScheduleDailyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (plan.getStatus() == ScheduleDailyPlan.Status.CANCELED) {
            throw new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_CANCELED);
        }
        scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .ifPresent(ss -> { throw new BaseException(ErrorCode.SCHEDULE_STUDENT_ALREADY_RESERVED); });

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDENT_NOT_FOUND));
        BusStop busStop = busStopRepository.findById(busStopId)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));

        ScheduleDailyPlanStudent ss = ScheduleDailyPlanStudent.builder()
                .scheduleDailyPlan(plan)
                .student(student)
                .date(plan.getDate())
                .busStop(busStop)
                .plannedTime(plannedTime)
                .scheduleStatus(ScheduleDailyPlanStudent.ScheduleStatus.RESERVED)
                .build();
        return scheduleStudentRepository.save(ss);
    }

    @Transactional
    public ScheduleDailyPlanStudent cancel(UUID planId, UUID studentId) {
        ScheduleDailyPlanStudent ss = scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        ss.update(null, null, ScheduleDailyPlanStudent.ScheduleStatus.CANCELED);
        return ss;
    }

    @Transactional
    public ScheduleDailyPlanStudent updateStatus(UUID planId, UUID studentId, ScheduleDailyPlanStudent.ScheduleStatus status) {
        ScheduleDailyPlanStudent ss = scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        ss.update(null, null, status);
        return ss;
    }

    @Transactional
    public ScheduleDailyPlanStudent updateAssignment(UUID planId, UUID studentId, UUID busStopId, LocalTime plannedTime) {
        ScheduleDailyPlanStudent ss = scheduleStudentRepository.findByScheduleDailyPlanIdAndStudentId(planId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        BusStop bs = null;
        if (busStopId != null) {
            bs = busStopRepository.findById(busStopId)
                    .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
        }
        ss.update(bs, plannedTime, null);
        return ss;
    }

    public List<ScheduleDailyPlanStudent> listByPlan(UUID planId) {
        return scheduleStudentRepository.findByScheduleDailyPlanId(planId);
    }

    public List<ScheduleDailyPlanStudent> listByStudent(UUID studentId, LocalDate from, LocalDate to) {
        return scheduleStudentRepository.findByStudentIdAndDateBetween(studentId, from, to);
    }
}
