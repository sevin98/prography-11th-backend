package com.prography11thbackend.api.session.controller;

import com.prography11thbackend.api.session.dto.SessionResponse;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private static final Long CURRENT_COHORT_ID = 11L; // 11기 고정

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessions(@RequestParam(required = false) Long cohortId) {
        Long targetCohortId = cohortId != null ? cohortId : CURRENT_COHORT_ID;
        List<SessionResponse> sessions = sessionService.getSessionsForMember(targetCohortId).stream()
                .map(SessionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }
}
