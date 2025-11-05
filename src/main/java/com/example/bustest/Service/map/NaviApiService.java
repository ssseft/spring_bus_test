package com.example.bustest.Service.map;

import com.example.bustest.dto.map.CoordinateDTO;
import com.example.bustest.dto.map.RouteResponse;
import com.example.bustest.dto.map.NaviResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Kakao Mobility Navi API(길찾기) 연동 서비스
 * - 문서: https://developers.kakaomobility.com/docs/navi-api/directions/
 * - endpoint 예: https://apis-navi.kakaomobility.com/v1/directions?origin=x,y&destination=x,y&waypoints=x,y|x,y
 * - 응답의 routes[].sections[].roads[].vertexes 에 도로 polyline 좌표가 x,y 반복으로 담깁니다.
 *
 * 주의
 * - Kakao 좌표는 x=경도(lng), y=위도(lat) 입니다. 화면/내부에서는 위도(lat)->y, 경도(lng)->x 순서로 맞춰줍니다.
 */
@Service
public class NaviApiService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // 별도 설정이 없으면 기존 REST KEY 를 그대로 사용
    @Value("${kakao.navi.rest-key:${kakao.api.rest-key}}")
    private String naviRestKey;

    @Value("${kakao.navi.directions-url:https://apis-navi.kakaomobility.com/v1/directions}")
    private String directionsUrl;

    /**
     * 순서가 보장된 좌표 목록(최소 2개)으로 길찾기 요청을 보내고, 그릴 수 있는 폴리라인과 요약값을 반환합니다.
     */
    public Optional<RouteResponse> directionsByOrderedCoords(List<CoordinateDTO> ordered) {
        try {
            if (ordered == null || ordered.size() < 2) return Optional.empty();

            // origin(첫 점), destination(마지막 점), 중간은 waypoints 로 직렬화
            CoordinateDTO origin = ordered.get(0);
            CoordinateDTO destination = ordered.get(ordered.size() - 1);

            String originParam = toXY(origin);
            String destinationParam = toXY(destination);

            String waypointsParam = null;
            if (ordered.size() > 2) {
                List<String> wp = new ArrayList<>();
                for (int i = 1; i < ordered.size() - 1; i++) {
                    wp.add(toXY(ordered.get(i)));
                }
                // x,y|x,y|x,y ... 형식
                waypointsParam = String.join("|", wp);
            }

            StringBuilder url = new StringBuilder(directionsUrl)
                    .append("?origin=").append(URLEncoder.encode(originParam, StandardCharsets.UTF_8))
                    .append("&destination=").append(URLEncoder.encode(destinationParam, StandardCharsets.UTF_8));
            if (waypointsParam != null && !waypointsParam.isBlank()) {
                url.append("&waypoints=")
                        .append(URLEncoder.encode(waypointsParam, StandardCharsets.UTF_8));
            }

            // 우선 기본 옵션(추천 경로)로 요청
            // 필요 시 priority, car_fuel 등 추가 가능
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + naviRestKey)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode routes = root.path("routes");
            if (!routes.isArray() || routes.isEmpty()) return Optional.empty();
            JsonNode firstRoute = routes.get(0);

            long distance = firstRoute.path("summary").path("distance").asLong(0);
            long duration = firstRoute.path("summary").path("duration").asLong(0);

            List<CoordinateDTO> path = new ArrayList<>();
            JsonNode sections = firstRoute.path("sections");
            if (sections.isArray()) {
                for (JsonNode sec : sections) {
                    JsonNode roads = sec.path("roads");
                    if (roads.isArray()) {
                        for (JsonNode road : roads) {
                            JsonNode vertexes = road.path("vertexes");
                            // vertexes 는 [x1, y1, x2, y2, ...] 로 평탄화된 배열
                            if (vertexes.isArray()) {
                                for (int i = 0; i + 1 < vertexes.size(); i += 2) {
                                    double x = vertexes.get(i).asDouble();     // 경도
                                    double y = vertexes.get(i + 1).asDouble(); // 위도
                                    path.add(new CoordinateDTO(y, x));
                                }
                            }
                        }
                    }
                }
            }

            return Optional.of(new RouteResponse(path, distance, duration));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 원본 JSON 포함 버전 (저장용)
     */
    public Optional<NaviResult> directionsWithRaw(List<CoordinateDTO> ordered) {
        try {
            if (ordered == null || ordered.size() < 2) return Optional.empty();

            CoordinateDTO origin = ordered.get(0);
            CoordinateDTO destination = ordered.get(ordered.size() - 1);

            String originParam = toXY(origin);
            String destinationParam = toXY(destination);

            String waypointsParam = null;
            if (ordered.size() > 2) {
                List<String> wp = new ArrayList<>();
                for (int i = 1; i < ordered.size() - 1; i++) {
                    wp.add(toXY(ordered.get(i)));
                }
                waypointsParam = String.join("|", wp);
            }

            StringBuilder url = new StringBuilder(directionsUrl)
                    .append("?origin=").append(URLEncoder.encode(originParam, StandardCharsets.UTF_8))
                    .append("&destination=").append(URLEncoder.encode(destinationParam, StandardCharsets.UTF_8));
            if (waypointsParam != null && !waypointsParam.isBlank()) {
                url.append("&waypoints=")
                        .append(URLEncoder.encode(waypointsParam, StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + naviRestKey)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode routes = root.path("routes");
            if (!routes.isArray() || routes.isEmpty()) return Optional.empty();

            JsonNode firstRoute = routes.get(0);
            long distance = firstRoute.path("summary").path("distance").asLong(0);
            long duration = firstRoute.path("summary").path("duration").asLong(0);

            List<CoordinateDTO> path = new ArrayList<>();
            JsonNode sections = firstRoute.path("sections");
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

            RouteResponse summary = new RouteResponse(path, distance, duration);
            return Optional.of(new NaviResult(summary, root));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String toXY(CoordinateDTO c) {
        // Kakao Navi 는 x(경도),y(위도) 순 매개변수를 요구
        return c.getLongitude() + "," + c.getLatitude();
    }
}
