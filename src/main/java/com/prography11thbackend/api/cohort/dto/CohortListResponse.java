package com.prography11thbackend.api.cohort.dto;

import com.prography11thbackend.domain.cohort.entity.Cohort;

import java.time.Instant;
import java.time.ZoneOffset;

public record CohortListResponse(
        Long id,
        Integer generation,
        String name,
        Instant createdAt
) {
    public static CohortListResponse from(Cohort cohort) {
        return new CohortListResponse(
                cohort.getId(),
                cohort.getNumber(),
                cohort.getNumber() + "기",
                cohort.getCreatedAt() != null ? cohort.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
