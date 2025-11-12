package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.ScheduleDailyPlanStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleDailyPlanStudentRepository extends JpaRepository<ScheduleDailyPlanStudent, UUID> {
    List<ScheduleDailyPlanStudent> findByScheduleDailyPlanId(UUID planId);
    Optional<ScheduleDailyPlanStudent> findByScheduleDailyPlanIdAndStudentId(UUID planId, UUID studentId);
    List<ScheduleDailyPlanStudent> findByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);
    int countByScheduleDailyPlanId(UUID planId); //panid개수반환
}