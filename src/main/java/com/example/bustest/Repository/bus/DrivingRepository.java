package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.Driving;
import com.example.bustest.domain.bus.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DrivingRepository extends JpaRepository<Driving, UUID> {
    Optional<Driving> findByScheduleAndDate(Schedule schedule, LocalDate date);
    List<Driving> findByScheduleIdAndDateBetween(UUID scheduleId, LocalDate from, LocalDate to);
}

