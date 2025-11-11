package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
}

