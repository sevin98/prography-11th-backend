package com.prography11thbackend.api.cohort.dto;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.Part;

import java.time.Instant;
import java.time.ZoneOffset;
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
        // parts는 Part enum에서 생성 (명시적 매핑으로 API 계약 보장: SERVER=6, WEB=7, iOS=8, ANDROID=9, DESIGN=10)
        List<PartResponse> parts = Stream.of(Part.values())
                .map(part -> {
                    Long partId = switch (part) {
                        case SERVER -> 6L;
                        case WEB -> 7L;
                        case iOS -> 8L;
                        case ANDROID -> 9L;
                        case DESIGN -> 10L;
                    };
                    return PartResponse.from(part, partId);
                })
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
                cohort.getCreatedAt() != null ? cohort.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
