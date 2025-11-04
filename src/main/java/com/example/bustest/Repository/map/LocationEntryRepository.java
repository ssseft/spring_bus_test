package com.example.bustest.Repository.map;

import com.example.bustest.domain.map.LocationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationEntryRepository extends JpaRepository<LocationEntry, Long> {
}

