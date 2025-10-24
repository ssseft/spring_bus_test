package com.example.bustest.domain.user;

import com.example.bustest.domain.common.Address;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 사용자 엔티티
 * 학원 관리자, 기사, 학생, 학부모 등 모든 사용자의 공통 정보를 관리
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 500)
    private String email;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Embedded
    private Address address;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Column(name = "notification_consent", nullable = false)
    private Boolean notificationConsent;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /**
     * 사용자 계정 상태
     */
    public enum UserStatus {
        ACTIVE,     // 활성
        INACTIVE,   // 비활성 (휴면 회원)
        SUSPENDED   // 정지 (악성 회원)
    }

    /**
     * 사용자 유형
     */
    public enum UserType {
        ACADEMY_MANAGER,  // 학원 관리자
        DRIVER,           // 기사
        STUDENT,          // 학생
        PARENT            // 학부모
    }

    /**
     * 알림 채널
     */
    public enum NotificationChannel {
        APP,    // 앱 푸시 알림
        SMS,    // 문자 메시지
        EMAIL   // 이메일
    }

    /**
     * 생성자 함수
     */
    @Builder
    public User(String phoneNumber, String login, String password, String name,
                String email, LocalDate birthDate, Address address,
                String profileImageUrl, UserType userType, Boolean notificationConsent) {
        this.phoneNumber = phoneNumber;
        this.login = login;
        this.password = password;
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.userType = userType;
        this.notificationConsent = notificationConsent;
        this.status = UserStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 사용자 정보 수정
     * - null이 아닌 필드만 업데이트 (부분 업데이트 지원)
     * - updatedAt 자동 갱신
     */
    public void update(String phoneNumber,String email,
                       Address address,String profileImageUrl, UserStatus status, Boolean notificationConsent) {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (email != null) {
            this.email = email;
        }
        if (address != null) {
            this.address = address;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        if (status != null) {
            this.status = status;
        }
        if (notificationConsent != null) {
            this.notificationConsent = notificationConsent;
        }

        this.updatedAt = Instant.now();
    }

}