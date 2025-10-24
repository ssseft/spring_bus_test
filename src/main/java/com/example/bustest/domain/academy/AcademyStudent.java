package com.example.bustest.domain.academy;

import com.example.bustest.domain.user.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 학생-학원 등록 엔티티
 * 학생과 학원 간의 등록 관계 및 상태를 관리
 */
@Entity
@Table(name = "academy_student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /**
     * 등록 상태
     */
    public enum EnrollmentStatus {
        ACTIVE,    // 활성
        INACTIVE   // 비활성 (휴원, 등원 중단)
    }
}