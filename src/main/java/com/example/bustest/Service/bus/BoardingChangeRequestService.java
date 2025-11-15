package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.*;
import com.example.bustest.Repository.map.RouteRepository;
import com.example.bustest.Service.map.RoutePersistService;
import com.example.bustest.domain.bus.*;
import com.example.bustest.dto.map.RouteCreateRequest;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class  BoardingChangeRequestService {

    private final BoardingChangeRequestRepository bcrRepository;
    private final RunRepository runRepository;
    private final RunStudentRepository runStudentRepository;
    private final BusStopRepository busStopRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final RoutePersistService routePersistService;

    @Transactional
    public BoardingChangeRequest request(UUID runId, UUID studentId, UUID toBusStopId, String reason) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (run.getStatus() != Run.RunStatus.scheduled) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }
        RunStudent rs = runStudentRepository.findByRunIdAndStudentId(runId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        if (rs.getStatus() != RunStudent.RunStudentStatus.reserved) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // prevent duplicate pending
        bcrRepository.findByRun_IdAndStudent_IdAndStatus(runId, studentId, BoardingChangeRequest.Status.pending)
                .ifPresent(x -> { throw new BaseException(ErrorCode.INVALID_INPUT_VALUE); });

        BusStop to = busStopRepository.findById(toBusStopId)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));

        BoardingChangeRequest req = BoardingChangeRequest.builder()
                .run(run)
                .student(rs.getStudent())
                .fromStop(rs.getBusStop())
                .toStop(to)
                .status(BoardingChangeRequest.Status.pending)
                .reason(reason)
                .build();
        return bcrRepository.save(req);
    }

    @Transactional
    public BoardingChangeRequest approve(UUID requestId, UUID processedBy) {
        BoardingChangeRequest req = bcrRepository.findById(requestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));

        Run run = req.getRun();
        if (run.getStatus() != Run.RunStatus.scheduled) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }
        RunStudent rs = runStudentRepository.findByRunIdAndStudentId(run.getId(), req.getStudent().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        if (rs.getStatus() != RunStudent.RunStudentStatus.reserved) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 1) update assignment
        rs.update(req.getToStop(), null, null);

        // 2) rebuild route if needed: replace fromStop with toStop in current route's order (unique)
        UUID currentRouteId = run.getRoute() != null ? run.getRoute().getId() : run.getSchedule().getRoute().getId();
        List<RouteStop> ordered = routeStopRepository.findByRoute_IdOrderByStopOrder(currentRouteId);
        UUID fromId = req.getFromStop().getId();
        UUID toId = req.getToStop().getId();

        LinkedHashSet<UUID> newOrderSet = new LinkedHashSet<>();
        for (RouteStop r : ordered) {
            UUID id = r.getBusStop().getId();
            if (id.equals(fromId)) {
                newOrderSet.add(toId); // swap position
            } else {
                newOrderSet.add(id);
            }
        }
        List<UUID> newOrder = new ArrayList<>(newOrderSet);
        if (newOrder.size() < 2) {
            req.approve(processedBy);
            return req;
        }

        // persist temporary route
        RouteCreateRequest rreq = new RouteCreateRequest();
        rreq.setName("TEMP-RUN-" + run.getId());
        rreq.setOrderedBusStopIds(newOrder);
        UUID academyId = run.getSchedule().getAcademyId();
        var resp = routePersistService.create(academyId, rreq);
        var newRoute = routeRepository.findById(resp.getRouteId())
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        run.update(null, newRoute);

        // remap planned times
        for (RunStudent other : runStudentRepository.findByRunId(run.getId())) {
            if (other.getStatus() == RunStudent.RunStudentStatus.canceled) continue;
            routeStopRepository.findByRoute_IdAndBusStop_Id(newRoute.getId(), other.getBusStop().getId())
                    .ifPresent(rsStop -> other.update(null, rsStop.getStartToArriveTime(), null));
        }

        req.approve(processedBy);
        return req;
    }

    @Transactional
    public BoardingChangeRequest reject(UUID requestId, UUID processedBy, String rejectReason) {
        BoardingChangeRequest req = bcrRepository.findById(requestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));
        req.reject(processedBy, rejectReason);
        return req;
    }

    public List<BoardingChangeRequest> listByRun(UUID runId) {
        return bcrRepository.findByRun_Id(runId);
    }
}

