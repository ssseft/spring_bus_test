package com.example.bustest.Service.map;

import com.example.bustest.dto.map.CoordinateDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class KakaoApiService {
    private final WebClient webClient; // 호환성 유지를 위해 보관(비동기 HTTP 클라이언트)
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${kakao.api.rest-key}")
    private String restApiKey;

    @Value("${kakao.api.geocoding-url:https://dapi.kakao.com/v2/local/search/address.json}")
    private String geocodingUrl;

    @Value("${kakao.api.keyword-url:https://dapi.kakao.com/v2/local/search/keyword.json}")
    private String keywordUrl;

    // 생성자에서 WebClient 주입
    public KakaoApiService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    // 주소 문자열을 좌표(위도/경도)로 변환
    // - Kakao Local REST API 호출
    // - 응답은 x=경도(longitude), y=위도(latitude)이므로 주의하여 매핑
    public Optional<CoordinateDTO> geocodeAddress(String address) {
        if (address == null || address.isBlank()) return Optional.empty();

        String query = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = geocodingUrl + "?query=" + query;

        // Authorization 헤더: "KakaoAK {REST_KEY}"
        Mono<String> responseMono = webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
                .retrieve()
                .bodyToMono(String.class);

        try {
            String body = responseMono.block();
            if (body == null) return Optional.empty();
            JsonNode root = objectMapper.readTree(body);
            JsonNode documents = root.path("documents");
            if (!documents.isArray() || documents.isEmpty()) return Optional.empty();
            JsonNode first = documents.get(0);
            double x = first.path("x").asDouble(); // 경도
            double y = first.path("y").asDouble(); // 위도
            return Optional.of(new CoordinateDTO(y, x));
        } catch (Exception e) {
            // 호출/파싱 실패 시 빈 Optional 반환
            return Optional.empty();
        }
    }

    // 주소 지오코딩이 실패하면 키워드 검색으로 보정 시도
    public Optional<CoordinateDTO> geocodeWithFallback(String addressOrKeyword) {
        Optional<CoordinateDTO> byAddress = geocodeAddress(addressOrKeyword);
        if (byAddress.isPresent()) return byAddress;
        return geocodeByEndpoint(keywordUrl, addressOrKeyword);
    }

    // Kakao Local API 호출 공통 처리 (주소/키워드 엔드포인트 공용)
    private Optional<CoordinateDTO> geocodeByEndpoint(String baseUrl, String queryText) {
        try {
            String query = URLEncoder.encode(queryText, StandardCharsets.UTF_8);
            String url = baseUrl + "?query=" + query;
            System.out.println("[KakaoApiService] calling: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                System.out.println("[KakaoApiService] non-2xx status: " + resp.statusCode());
                return Optional.empty();
            }
            String body = resp.body();
            if (body == null || body.isBlank()) return Optional.empty();
            JsonNode root = objectMapper.readTree(body);
            JsonNode documents = root.path("documents");
            if (!documents.isArray() || documents.isEmpty()) return Optional.empty();
            JsonNode first = documents.get(0);
            double x = first.path("x").asDouble();
            dou
            ble y = first.path("y").asDouble();
            if (Double.isNaN(x) || Double.isNaN(y)) return Optional.empty();
            return Optional.of(new CoordinateDTO(y, x));
        } catch (Exception e) {
            System.out.println("[KakaoApiService] error: " + e);
            return Optional.empty();
        }
    }

    // 주소 검증: 주소/키워드 어느 쪽이든 결과가 있으면 true
    public boolean validateAddressOrKeyword(String address) {
        if (address == null || address.isBlank()) return false;
        if (geocodeAddress(address).isPresent()) return true;
        return geocodeByEndpoint(keywordUrl, address).isPresent();
    }
}