package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.BoardingChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardingChangeRequestRepository extends JpaRepository<BoardingChangeRequest, UUID> {
    List<BoardingChangeRequest> findByRun_Id(UUID runId);
    List<BoardingChangeRequest> findByRun_IdAndStudent_Id(UUID runId, UUID studentId);
    Optional<BoardingChangeRequest> findByRun_IdAndStudent_IdAndStatus(UUID runId, UUID studentId, BoardingChangeRequest.Status status);
}

