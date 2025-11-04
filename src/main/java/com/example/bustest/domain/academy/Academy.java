package com.example.bustest.domain.academy;

import com.example.bustest.domain.common.Address;
import com.example.bustest.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import com.example.bustest.exception.BaseException;
import com.example.bustest.exception.ErrorCode;
/**
 * 학원 엔티티
 * 학원의 기본 정보와 운영 정보를 관리
 */
@Entity
@Table(name = "academies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Academy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = false, length = 500)
    private String email;

    @Embedded
    private Address address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User repManager;

    @Column(name = "logo_url", nullable = false, length = 1000)
    private String logoUrl;

    @ElementCollection
    @OrderBy("operationType ASC")
    List<OperationInfo> operationInfos;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "geom", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point geom; // service에서 create할 때 계산하고 넣을 예정

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();


    /**
     * 생성자 함수
     */
    @Builder
    public Academy(String name, String phoneNumber, String email, Address address, User repManager, String logoUrl, List<OperationInfo> operationInfos, Point geom) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.repManager = repManager;
        this.logoUrl = logoUrl;
        this.operationInfos = operationInfos;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        if (geom == null) throw new BaseException(ErrorCode.BUS_SOP_DESTINATION_NOT_FOUND);
        if (geom.getSRID() != 4326) geom.setSRID(4326);
        this.geom = geom;
    }

    public void setGeom(Point geom) {
        if (geom == null) throw new BaseException(ErrorCode.BUS_SOP_DESTINATION_NOT_FOUND);
        if (geom.getSRID() != 4326) geom.setSRID(4326);
        this.geom = geom;
        this.updatedAt = Instant.now();
    }

    // 파생 getter: geom은 NOT NULL이므로 바로 반환
    public BigDecimal getLatitude() {
        return BigDecimal.valueOf(geom.getY());
    }

    public BigDecimal getLongitude() {
        return BigDecimal.valueOf(geom.getX());
    }

    /**
     *  학원 정보 수정 함수
     *  - null이 아닌 필드만 업데이트 (부분 업데이트 지원)
     *  - updatedAt 자동 갱신
     */
    public void updateAcademy(String name, String phoneNumber, String email, Address address, User repManager, String logoUrl, List<OperationInfo> operationInfos) {
        if (name != null) {
            this.name = name;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (email != null) {
            this.email = email;
        }
        if (address != null) {
            this.address = address;
        }
        if (repManager != null) {
            this.repManager = repManager;
        }
        if (logoUrl != null) {
            this.logoUrl = logoUrl;
        }
        if (operationInfos != null) {
            this.operationInfos = operationInfos;
        }

        this.updatedAt = Instant.now();
    }

    /**
     * 학원 대표 관리자 삭제
     * - 학원 대표 관리자 계정 삭제 시, 학원 대표 관리자도 함께 삭제
     */

    public void deleteRepManager() {
        this.repManager = null;
        updatedAt = Instant.now();
    }
}
