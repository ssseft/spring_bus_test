package com.example.bustest.domain.user;

import com.example.bustest.domain.academy.Academy;
import com.example.bustest.domain.common.Address;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 학원 관리자 엔티티
 * 학원 소속 관리자 정보를 관리
 */
@Entity
@Table(name = "academy_managers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyManager {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private
    Academy academy;


    @Builder
    public AcademyManager(User user, Academy academy) {
        this.user = user;
        this.academy = academy;
    }

    /**
     * 학원 관리자 정보 수정
     * - null이 아닌 필드만 업데이트 (부분 업데이트 지원)
     */
    public void updateAcademyManager(String phoneNumber, String email,
                                     Address address, String profileImageUrl, User.UserStatus status, Boolean notificationConsent) {
        this.user.update(phoneNumber, email, address, profileImageUrl, status, notificationConsent);
    }
}