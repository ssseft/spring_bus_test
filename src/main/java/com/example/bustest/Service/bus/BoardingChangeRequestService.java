package com.example.bustest.Service.bus;

import com.example.bustest.Repository.bus.*;
import com.example.bustest.Repository.bus.RouteRepository;
import com.example.bustest.domain.bus.*;
import com.example.bustest.dto.map.RouteCreateRequest;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 서비스: 승하차 정류장 변경 요청(학생별) 생성/승인/거절 및 조회.
 * - request(drivingId, studentId, toBusStopId, reason): 변경 요청 생성(중복 pending 방지)
 * - approve(requestId, processedBy): 요청 승인 → DrivingStudent 정류장 교체 + 필요 시 임시 노선 재생성 + 도착시간 리매핑
 * - reject(requestId, processedBy, rejectReason): 요청 거절
 * - listByDriving(drivingId): 운행(driving) 단위 요청 목록
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardingChangeRequestService {

    private final BoardingChangeRequestRepository bcrRepository;
    private final com.example.bustest.Repository.bus.DrivingRepository runRepository;
    private final com.example.bustest.Repository.bus.DrivingStudentRepository runStudentRepository;
    private final BusStopRepository busStopRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final RoutePersistService routePersistService;

    /**
     * 변경 요청 생성
     * - 운행 상태가 scheduled이고, 학생 예약 상태가 reserved인 경우에만 가능
     * - 동일 Run/Student 조합으로 pending 상태가 이미 존재하면 거부
     *
     * @param drivingId   대상 운행 ID
     * @param studentId   대상 학생 ID
     * @param toBusStopId 변경하려는 도착 정류장 ID
     * @param reason      사유(선택)
     * @return 생성된 요청 엔티티(pending)
     */
    @Transactional
    public BoardingChangeRequest request(UUID drivingId, UUID studentId, UUID toBusStopId, String reason) {
        Driving driving = getScheduledDrivingOrThrow(drivingId);
        DrivingStudent reservation = getReservedDrivingStudentOrThrow(drivingId, studentId);

        // 동일 학생의 중복 pending 방지
        bcrRepository.findByDriving_IdAndStudent_IdAndStatus(drivingId, studentId, BoardingChangeRequest.Status.pending)
                .ifPresent(x -> { throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "duplicate pending request"); });

        BusStop to = busStopRepository.findById(toBusStopId)
                .orElseThrow(() -> new BaseException(ErrorCode.BUS_STOP_NOT_FOUND));

        BoardingChangeRequest req = BoardingChangeRequest.builder()
                .driving(driving)
                .student(reservation.getStudent())
                .fromStop(reservation.getBusStop())
                .toStop(to)
                .status(BoardingChangeRequest.Status.pending)
                .reason(reason)
                .build();
        return bcrRepository.save(req);
    }

    /**
     * 요청 승인 처리
     * - DrivingStudent의 정류장을 교체하고, 필요 시 임시 노선을 재생성하여 Driving에 반영
     * - 새 노선의 구간 도착 시간으로 DrivingStudent들의 plannedTime을 리매핑
     *
     * @param requestId   요청 ID
     * @param processedBy 처리자(승인자) ID
     * @return 승인된 요청 엔티티
     */
    @Transactional
    public BoardingChangeRequest approve(UUID requestId, UUID processedBy) {
        BoardingChangeRequest req = bcrRepository.findById(requestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));

        Driving driving = getScheduledDrivingOrThrow(req.getDriving().getId());
        DrivingStudent rs = getReservedDrivingStudentOrThrow(driving.getId(), req.getStudent().getId());

        // 1) 학생 배정 정류장 교체(시간/상태는 보존)
        rs.update(req.getToStop(), null, null);

        // 2) 필요 시 노선 재생성: 기존 순서에서 fromStop → toStop 교체
        UUID currentRouteId = (driving.getRoute() != null) ? driving.getRoute().getId() : driving.getSchedule().getRoute().getId();
        List<RouteStop> ordered = routeStopRepository.findByRoute_IdOrderByStopOrder(currentRouteId);

        List<UUID> newOrder = buildSwappedOrder(ordered, req.getFromStop().getId(), req.getToStop().getId());
        if (newOrder.size() < 2) { // 노선이 성립하지 않으면 경로 재생성 생략
            req.approve(processedBy);
            return req;
        }

        // 임시 노선 생성 후 Driving에 반영
        RouteCreateRequest rreq = new RouteCreateRequest();
        rreq.setName("TEMP-DRIVING-" + driving.getId());
        rreq.setOrderedBusStopIds(newOrder);
        UUID academyId = driving.getSchedule().getAcademyId();
        var resp = routePersistService.create(academyId, rreq);
        var newRoute = routeRepository.findById(resp.getRouteId())
                .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
        driving.update(null, newRoute);

        // 3) 새 노선 기준 planned 도착시간 리매핑
        remapPlannedTimes(driving, newRoute);

        req.approve(processedBy);
        return req;
    }

    /**
     * 요청 거절 처리
     * @param requestId    요청 ID
     * @param processedBy  처리자 ID
     * @param rejectReason 거절 사유(선택)
     * @return 거절된 요청 엔티티
     */
    @Transactional
    public BoardingChangeRequest reject(UUID requestId, UUID processedBy, String rejectReason) {
        BoardingChangeRequest req = bcrRepository.findById(requestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));
        req.reject(processedBy, rejectReason);
        return req;
    }

    /**
     * 특정 운행(driving) 단위의 변경 요청 목록 조회
     */
    public List<BoardingChangeRequest> listByDriving(UUID drivingId) {
        return bcrRepository.findByDriving_Id(drivingId);
    }

    // ====== 내부 헬퍼 ======

    private Driving getScheduledDrivingOrThrow(UUID drivingId) {
        Driving driving = runRepository.findById(drivingId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_DAILY_PLAN_NOT_FOUND));
        if (driving.getStatus() != Driving.drivingStatus.scheduled) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "driving is not scheduled");
        }
        return driving;
    }

    private DrivingStudent getReservedDrivingStudentOrThrow(UUID drivingId, UUID studentId) {
        DrivingStudent rs = runStudentRepository.findByDriving_IdAndStudent_Id(drivingId, studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_STUDENT_RESERVATION_NOT_FOUND));
        if (rs.getStatus() != DrivingStudent.drivingStudentStatus.reserved) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "reservation is not reserved");
        }
        return rs;
    }

    /**
     * 기존 노선 순서에서 fromStop을 toStop으로 치환한 순서를 생성(중복 제거, 순서 보존)
     */
    private List<UUID> buildSwappedOrder(List<RouteStop> ordered, UUID fromStopId, UUID toStopId) {
        LinkedHashSet<UUID> set = new LinkedHashSet<>();
        for (RouteStop r : ordered) {
            UUID id = r.getBusStop().getId();
            set.add(id.equals(fromStopId) ? toStopId : id);
        }
        return new ArrayList<>(set);
    }

    /**
     * 새 노선의 RouteStop 도착 시간으로 Driving 내 학생들의 plannedTime을 재설정
     */
    private void remapPlannedTimes(Driving driving, Route newRoute) {
        List<DrivingStudent> students = runStudentRepository.findByDriving_Id(driving.getId());
        for (DrivingStudent other : students) {
            if (other.getStatus() == DrivingStudent.drivingStudentStatus.canceled) continue;
            routeStopRepository.findByRoute_IdAndBusStop_Id(newRoute.getId(), other.getBusStop().getId())
                    .ifPresent(rsStop -> other.update(null, rsStop.getStartToArriveTime(), null));
        }
    }
}
