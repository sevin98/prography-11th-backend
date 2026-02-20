package com.prography11thbackend.api.session.controller;

import com.prography11thbackend.api.session.dto.SessionCreateRequest;
import com.prography11thbackend.api.session.dto.SessionResponse;
import com.prography11thbackend.api.session.dto.SessionUpdateRequest;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/sessions")
@RequiredArgsConstructor
public class AdminSessionController {

    private final SessionService sessionService;
    private static final Long CURRENT_COHORT_ID = 11L; // 11기 고정

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessions(@RequestParam(required = false) Long cohortId) {
        Long targetCohortId = cohortId != null ? cohortId : CURRENT_COHORT_ID;
        List<SessionResponse> sessions = sessionService.getSessionsForAdmin(targetCohortId).stream()
                .map(SessionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionCreateRequest request) {
        Long cohortId = request.cohortId() != null ? request.cohortId() : CURRENT_COHORT_ID;
        Session session = sessionService.createSession(
                request.title(),
                request.description(),
                request.startTime(),
                cohortId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SessionResponse.from(session));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionResponse> updateSession(@PathVariable Long id, @RequestBody SessionUpdateRequest request) {
        Session session = sessionService.updateSession(
                id,
                request.title(),
                request.description(),
                request.startTime()
        );
        return ResponseEntity.ok(SessionResponse.from(session));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
