package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.Run;
import com.example.bustest.domain.bus.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RunRepository extends JpaRepository<Run, UUID> {
    Optional<Run> findByScheduleAndDate(Schedule schedule, LocalDate date);
    List<Run> findByScheduleIdAndDateBetween(UUID scheduleId, LocalDate from, LocalDate to);
}

