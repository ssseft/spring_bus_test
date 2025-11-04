package com.example.bustest.Controller.map;

import com.example.bustest.Repository.map.LocationEntryRepository;
import com.example.bustest.Service.map.KakaoApiService;
import com.example.bustest.domain.map.LocationEntry;
import com.example.bustest.dto.map.LocationCreateRequest;
import com.example.bustest.dto.map.LocationMarkerDTO;
import com.example.bustest.dto.map.LocationUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping({"/api/locations"})
@RequiredArgsConstructor
public class LocationController {
    private final LocationEntryRepository repository;
    private final KakaoApiService kakaoApiService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid LocationCreateRequest req) {
        return (ResponseEntity)this.kakaoApiService.geocodeWithFallback(req.getAddress()).map((coord) -> {
            LocationEntry saved = (LocationEntry)this.repository.save(new LocationEntry(req.getTitle(), req.getAddress(), coord.getLatitude(), coord.getLongitude(), req.getType()));
            return ResponseEntity.created(URI.create("/api/locations/" + saved.getId())).build();
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Address geocoding failed"));
    }

    @GetMapping
    public List<LocationMarkerDTO> list() {
        return this.repository.findAll().stream().filter((le) -> le.getLatitude() != null && le.getLongitude() != null).map((le) -> new LocationMarkerDTO(le.getId(), le.getTitle(), le.getLatitude(), le.getLongitude(), le.getType())).toList();
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody LocationUpdateRequest req) {
        return (ResponseEntity)this.repository.findById(id).map((entity) -> {
            if (req.getTitle() != null && !req.getTitle().isBlank()) {
                entity.setTitle(req.getTitle());
            }

            if (req.getType() != null && !req.getType().isBlank()) {
                entity.setType(req.getType());
            }

            this.repository.save(entity);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        return (ResponseEntity)this.repository.findById(id).map((entity) -> {
            this.repository.delete(entity);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping({"/validate"})
    public ResponseEntity<?> validate(@RequestParam("address") String address) {
        boolean ok = this.kakaoApiService.validateAddressOrKeyword(address);
        return ResponseEntity.ok().body("{\"valid\":" + ok + "}");
    }
}
