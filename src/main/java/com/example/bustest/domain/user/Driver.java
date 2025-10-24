package com.example.bustest.domain.user;

import com.example.bustest.domain.academy.Academy;
import com.example.bustest.domain.common.Address;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 기사 엔티티
 * 학원 소속 기사의 면허 정보 및 검증 상태를 관리
 */
@Entity
@Table(name = "drivers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @Column(name = "license_number", unique = true, nullable = false, length = 50)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_verification", nullable = false, length = 20)
    private LicenseVerification licenseVerification = LicenseVerification.UNVERIFIED;

    /**
     * 면허 검증 상태
     */
    public enum LicenseVerification {
        UNVERIFIED,  // 미검증
        PENDING,     // 검증 대기
        VERIFIED,    // 검증 완료
        EXPIRED,     // 만료
        DENIED       // 거부
    }

    @Builder
    public Driver(User user, Academy academy, String licenseNumber,
                  LicenseVerification licenseVerification) {
        this.user = user;
        this.academy = academy;
        this.licenseNumber = licenseNumber;
        this.licenseVerification =
                (licenseVerification == null ? LicenseVerification.UNVERIFIED : licenseVerification);
    }


    /**
     * 기사 정보 수정
     * - User 및 Driver 갱신
     */
    public void updateDriver(String phoneNumber,
                             String email,
                             Address address,
                             String profileImageUrl,
                             User.UserStatus status,
                             Boolean notificationConsent,
                             Academy academy,
                             String licenseNumber,
                             LicenseVerification licenseVerification) {

        this.user.update(phoneNumber, email, address, profileImageUrl, status, notificationConsent);

        // Driver 고유 필드 갱신
        this.academy = academy;
        this.licenseNumber = licenseNumber;
        this.licenseVerification = licenseVerification;
    }
}