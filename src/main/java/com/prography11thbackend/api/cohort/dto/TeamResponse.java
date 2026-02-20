package com.prography11thbackend.api.cohort.dto;

import com.prography11thbackend.domain.cohort.entity.Team;

import java.util.List;

public record TeamResponse(
        Long id,
        String name
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName()
        );
    }
}
