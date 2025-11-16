package com.example.bustest.Controller.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StudentController {

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/students")
    public List<StudentSummaryResponse> listAll(@RequestParam(required = false) UUID academyId) {
        // 현재 Student가 academy와 직접 매핑이 없어 전체 조회를 반환
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("SELECT s.id, u.name FROM students s JOIN users u ON s.user_id = u.id ORDER BY u.name ASC")
                .getResultList();
        return rows.stream()
                .map(r -> new StudentSummaryResponse((UUID) r[0], (String) r[1]))
                .toList();
    }

    @Getter
    @AllArgsConstructor
    public static class StudentSummaryResponse {
        private UUID id;
        private String name;
    }
}
