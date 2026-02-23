package com.prography11thbackend.api.cohort.dto;

import com.prography11thbackend.domain.cohort.entity.Part;

public record PartResponse(
        Long id,
        String name
) {
    public static PartResponse from(Part part, Long id) {
        return new PartResponse(
                id,
                part.name()
        );
    }
}
