package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.ScheduleStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleStudentRepository extends JpaRepository<ScheduleStudent, UUID> {
    List<ScheduleStudent> findByScheduleId(UUID scheduleId);
    Optional<ScheduleStudent> findByScheduleIdAndStudentId(UUID scheduleId, UUID studentId);
}
