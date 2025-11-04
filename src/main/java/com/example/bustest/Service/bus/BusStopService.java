package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.BusStopRepository;
import com.example.bustest.domain.bus.BusStop;
import com.example.bustest.dto.bus.BusStopCreateRequest;
import com.example.bustest.dto.bus.BusStopSummaryResponse;
import com.example.bustest.dto.bus.BusStopUpdateRequest;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusStopService {

    private final BusStopRepository busStopRepository;

    @Transactional
    public BusStopSummaryResponse create(BusStopCreateRequest req) {
        BusStop saved = busStopRepository.save(req.toEntity());
        return BusStopSummaryResponse.from(saved);
    }

    public BusStopSummaryResponse get(UUID id) {
        BusStop bs = busStopRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
        return BusStopSummaryResponse.from(bs);
    }

    public List<BusStopSummaryResponse> listByAcademy(UUID academyId) {
        return busStopRepository.findByAcademyId(academyId)
                .stream().map(BusStopSummaryResponse::from).toList();
    }

    public Page<BusStopSummaryResponse> pageByAcademy(UUID academyId, Pageable pageable) {
        return busStopRepository.findByAcademyId(academyId, pageable)
                .map(BusStopSummaryResponse::from);
    }

    @Transactional
    public BusStopSummaryResponse update(UUID id, BusStopUpdateRequest req) {
        BusStop bs = busStopRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));
        Point point = null;
        if (req.getLatitude() != null && req.getLongitude() != null) {
            GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
            point = gf.createPoint(new Coordinate(
                    req.getLongitude().doubleValue(),
                    req.getLatitude().doubleValue()
            ));
            point.setSRID(4326);
        }

        bs.update(
                req.getName(),
                point,
                req.getPhotoUrl(),
                req.getIsActive()
        );
        return BusStopSummaryResponse.from(bs);
    }

    @Transactional
    public void delete(UUID id) {
        if (!busStopRepository.existsById(id)) {
            throw new BaseException(ErrorCode.BUS_STOP_NOT_FOUND);
        }
        busStopRepository.deleteById(id);
    }
}
