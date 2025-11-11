package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.Schedule;
import com.example.bustest.domain.bus.ScheduleDailyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleDailyPlanRepository extends JpaRepository<ScheduleDailyPlan, UUID> {
    Optional<ScheduleDailyPlan> findByScheduleAndDate(Schedule schedule, LocalDate date);
    List<ScheduleDailyPlan> findByScheduleIdAndDateBetween(UUID scheduleId, LocalDate from, LocalDate to);
}