package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.RunStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RunStudentRepository extends JpaRepository<RunStudent, UUID> {
    List<RunStudent> findByRunId(UUID runId);
    Optional<RunStudent> findByRunIdAndStudentId(UUID runId, UUID studentId);
    List<RunStudent> findByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);
    int countByRunId(UUID runId);

    // Same-stop remaining passengers (exists/count)
    boolean existsByRunIdAndBusStopIdAndStatusInAndStudentIdNot(UUID runId, UUID busStopId, java.util.Collection<RunStudent.RunStudentStatus> statuses, UUID studentId);
    int countByRunIdAndBusStopIdAndStatusIn(UUID runId, UUID busStopId, java.util.Collection<RunStudent.RunStudentStatus> statuses);
}
