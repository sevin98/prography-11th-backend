package com.prography11thbackend.api.cohort.controller;

import com.prography11thbackend.api.cohort.dto.CohortResponse;
import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.service.CohortService;
import com.prography11thbackend.global.common.ApiResponse;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/cohorts")
@RequiredArgsConstructor
public class AdminCohortController {

    private final CohortService cohortService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<com.prography11thbackend.api.cohort.dto.CohortListResponse>>> getAllCohorts() {
        List<com.prography11thbackend.api.cohort.dto.CohortListResponse> cohorts = cohortService.getAllCohorts().stream()
                .map(com.prography11thbackend.api.cohort.dto.CohortListResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(cohorts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CohortResponse>> getCohort(@PathVariable Long id) {
        Cohort cohort = cohortService.getCohortById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(CohortResponse.from(cohort)));
    }
}
