package com.example.bustest.Repository.map;

import com.example.bustest.domain.bus.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

}
