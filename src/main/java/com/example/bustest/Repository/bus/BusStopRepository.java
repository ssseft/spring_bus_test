package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface BusStopRepository extends JpaRepository<BusStop, UUID> {
    List<BusStop> findByAcademyId(UUID academyId);
    Page<BusStop> findByAcademyId(UUID academyId, Pageable pageable);
}