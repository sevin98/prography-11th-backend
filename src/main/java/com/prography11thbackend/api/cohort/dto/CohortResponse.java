package com.prography11thbackend.api.cohort.dto;

import com.prography11thbackend.domain.cohort.entity.Cohort;

import java.util.List;

public record CohortResponse(
        Long id,
        Integer number,
        List<TeamResponse> teams
) {
    public static CohortResponse from(Cohort cohort) {
        return new CohortResponse(
                cohort.getId(),
                cohort.getNumber(),
                cohort.getTeams().stream()
                        .map(TeamResponse::from)
                        .toList()
        );
    }
}
