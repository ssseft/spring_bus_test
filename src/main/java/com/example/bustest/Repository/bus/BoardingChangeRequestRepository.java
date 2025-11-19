package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.BoardingChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardingChangeRequestRepository extends JpaRepository<BoardingChangeRequest, UUID> {
    List<BoardingChangeRequest> findByDriving_Id(UUID drivingId);
    List<BoardingChangeRequest> findByDriving_IdAndStudent_Id(UUID drivingId, UUID studentId);
    Optional<BoardingChangeRequest> findByDriving_IdAndStudent_IdAndStatus(UUID drivingId, UUID studentId, BoardingChangeRequest.Status status);
}

