package com.example.bustest.Repository.user;

import com.example.bustest.domain.user.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
}

