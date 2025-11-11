package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.ScheduleStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleStudentRepository extends JpaRepository<ScheduleStudent, UUID> {
    List<ScheduleStudent> findByScheduleDailyPlanId(UUID planId);
    Optional<ScheduleStudent> findByScheduleDailyPlanIdAndStudentId(UUID planId, UUID studentId);
    List<ScheduleStudent> findByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);
    long countByScheduleDailyPlanId(UUID planId);
}