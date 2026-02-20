package com.prography11thbackend.api.session.dto;

import java.time.LocalDateTime;

public record SessionCreateRequest(
        String title,
        String description,
        LocalDateTime startTime,
        Long cohortId
) {
}
