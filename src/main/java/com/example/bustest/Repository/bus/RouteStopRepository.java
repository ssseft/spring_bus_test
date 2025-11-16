package com.example.bustest.Repository.bus;

import com.example.bustest.domain.bus.RouteStop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// repository jpa 형식은 잘 몰라서 gpt한테 써달라고 했음
public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
    // Match by foreign key ids to avoid type mismatch with entity references
    boolean existsByRoute_IdAndBusStop_Id(UUID routeId, UUID busStopId);
    List<RouteStop> findByRoute_IdOrderByStopOrder(UUID routeId);

    //지연로딩으로 설정된걸 한번에 가져오기 위해 페치 조인 사용(이게 제일 편한듯)
    //이거 안쓰고 busstop테이블 가져오고 싶으면 직접 쿼리 작성해야 함
    @EntityGraph(attributePaths = {"busStop"})
    List<RouteStop> findWithBusStopByRoute_IdOrderByStopOrder(UUID routeId);

    Optional<RouteStop> findByRoute_IdAndBusStop_Id(UUID routeId, UUID busStopId);
}
