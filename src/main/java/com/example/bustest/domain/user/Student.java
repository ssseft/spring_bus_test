package com.example.bustest.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 학생 엔티티
 * 학생의 학교 정보 및 비상연락처를 관리
 */
@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_type", nullable = false)
    private SchoolType schoolType;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "class", nullable = false)
    private String classNumber;

    @Column(name = "secondary_contact", nullable = false)
    private String secondaryContact;

    /**
     * 학교 구분
     */
    public enum SchoolType {
        ELEMENTARY,  // 초등
        MIDDLE,      // 중등
        HIGH,        // 고등
        ETC          // 소속 학교가 특수하거나, 소속 학교가 없는 경우 등
    }
}