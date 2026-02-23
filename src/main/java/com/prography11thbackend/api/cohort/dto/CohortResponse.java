package com.prography11thbackend.api.cohort.dto;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.Part;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CohortResponse(
        Long id,
        Integer generation,
        String name,
        List<PartResponse> parts,
        List<TeamResponse> teams,
        Instant createdAt
) {
    public static CohortResponse from(Cohort cohort) {
        // parts는 Part enum에서 생성 (각 Part에 대해 id는 ordinal + 6: SERVER=6, WEB=7, iOS=8, ANDROID=9, DESIGN=10)
        List<PartResponse> parts = Stream.of(Part.values())
                .map(part -> PartResponse.from(part, (long) (part.ordinal() + 6)))
                .collect(Collectors.toList());
        
        List<TeamResponse> teams = cohort.getTeams().stream()
                .map(TeamResponse::from)
                .collect(Collectors.toList());
        
        return new CohortResponse(
                cohort.getId(),
                cohort.getNumber(),
                cohort.getNumber() + "기",
                parts,
                teams,
                cohort.getCreatedAt() != null ? cohort.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
