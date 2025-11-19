package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.DrivingStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DrivingStudentRepository extends JpaRepository<DrivingStudent, UUID> {
    List<DrivingStudent> findByDriving_Id(UUID drivingId);
    Optional<DrivingStudent> findByDriving_IdAndStudent_Id(UUID drivingId, UUID studentId);
    List<DrivingStudent> findByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);
    int countByDriving_Id(UUID drivingId);

    boolean existsByDriving_IdAndBusStopIdAndStatusInAndStudentIdNot(UUID drivingId, UUID busStopId, java.util.Collection<DrivingStudent.drivingStudentStatus> statuses, UUID studentId);
    int countByDriving_IdAndBusStopIdAndStatusIn(UUID drivingId, UUID busStopId, java.util.Collection<DrivingStudent.drivingStudentStatus> statuses);
}
