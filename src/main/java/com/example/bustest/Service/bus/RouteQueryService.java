package com.example.bustest.Service.bus;

import com.example.bustest.dto.map.CoordinateDTO;
import com.example.bustest.dto.map.RouteDetailResponse;
import com.example.bustest.dto.map.RouteSummaryResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RouteQueryService {
    @PersistenceContext
    private EntityManager em;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RouteSummaryResponse> listByAcademy(UUID academyId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("SELECT id, name, total_time, created_at FROM routes WHERE academy_id = ?1 ORDER BY created_at DESC")
                .setParameter(1, academyId)
                .getResultList();
        List<RouteSummaryResponse> out = new ArrayList<>();
        for (Object[] r : rows) {
            UUID id = (UUID) r[0];
            String name = (String) r[1];
            Object timeObj = r[2];
            Object createdObj = r[3];

            long secs = toSeconds(timeObj);
            Instant createdAt = toInstant(createdObj);

            out.add(new RouteSummaryResponse(id, name, secs, createdAt));
        }
        return out;
    }

    public RouteDetailResponse getDetail(UUID routeId) {
        Object[] row = (Object[]) em.createNativeQuery("SELECT id, name, total_time, nav_response::text FROM routes WHERE id = :id")
                .setParameter("id", routeId)
                .getSingleResult();
        if (row == null) return null;
        UUID id = (UUID) row[0];
        String name = (String) row[1];
        Object timeObj = row[2];
        String navText = (String) row[3];
        long totalSecs = toSeconds(timeObj);
        long distance = 0;
        long duration = 0;
        List<CoordinateDTO> path = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(navText);
            JsonNode routes = root.path("routes");
            if (routes.isArray() && !routes.isEmpty()) {
                JsonNode first = routes.get(0);
                distance = first.path("summary").path("distance").asLong(0);
                duration = first.path("summary").path("duration").asLong(0);
                JsonNode sections = first.path("sections");
                if (sections.isArray()) {
                    for (JsonNode sec : sections) {
                        JsonNode roads = sec.path("roads");
                        if (roads.isArray()) {
                            for (JsonNode road : roads) {
                                JsonNode vertexes = road.path("vertexes");
                                if (vertexes.isArray()) {
                                    for (int i = 0; i + 1 < vertexes.size(); i += 2) {
                                        double x = vertexes.get(i).asDouble();
                                        double y = vertexes.get(i + 1).asDouble();
                                        path.add(new CoordinateDTO(y, x));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return new RouteDetailResponse(id, name, totalSecs, distance, duration, path);
    }

    private Instant toInstant(Object o) {
        if (o == null) return null;
        if (o instanceof Instant i) return i;
        if (o instanceof Timestamp ts) return ts.toInstant();
        if (o instanceof OffsetDateTime odt) return odt.toInstant();
        if (o instanceof LocalDateTime ldt) return ldt.toInstant(ZoneOffset.UTC);
        try {
            return Instant.parse(String.valueOf(o));
        } catch (Exception ignored) {
            return null;
        }
    }

    private long toSeconds(Object timeObj) {
        if (timeObj == null) return 0;
        if (timeObj instanceof Time t) return t.toLocalTime().toSecondOfDay();
        if (timeObj instanceof LocalTime lt) return lt.toSecondOfDay();
        // Fallback: 0
        return 0;
    }
}
