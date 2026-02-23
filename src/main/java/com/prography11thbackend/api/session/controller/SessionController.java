package com.prography11thbackend.api.session.controller;

import com.prography11thbackend.api.session.dto.SessionBasicResponse;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.session.service.SessionService;
import com.prography11thbackend.global.common.ApiResponse;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final CohortRepository cohortRepository;
    private static final Integer CURRENT_COHORT_NUMBER = 11; // 11기 고정
    
    private Long getCurrentCohortId() {
        return cohortRepository.findByNumber(CURRENT_COHORT_NUMBER)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND))
                .getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionBasicResponse>>> getSessions() {
        List<SessionBasicResponse> sessions = sessionService.getSessionsForMember(getCurrentCohortId()).stream()
                .map(SessionBasicResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }
}
