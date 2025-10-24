package com.example.bustest.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 학부모-학생 관계 엔티티
 * 학부모와 학생 간의 관계 유형 및 검증 상태를 관리
 */
@Entity
@Table(name = "parent_student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 20)
    private Relationship relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification", nullable = false, length = 20)
    private Verification verification = Verification.UNVERIFIED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /**
     * 관계 유형
     */
    public enum Relationship {
        FATHER,    // 아버지
        MOTHER,    // 어머니
        GUARDIAN   // 보호자
    }

    /**
     * 검증 상태
     */
    public enum Verification {
        UNVERIFIED,  // 미검증
        PENDING,     // 검증 대기
        VERIFIED,    // 검증 완료
        DENIED       // 거부
    }
}