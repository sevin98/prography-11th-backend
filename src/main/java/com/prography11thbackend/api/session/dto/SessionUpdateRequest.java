package com.prography11thbackend.api.session.dto;

import java.time.LocalDateTime;

public record SessionUpdateRequest(
        String title,
        String description,
        LocalDateTime startTime
) {
}
