package com.prography11thbackend.api.session.dto;

import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;

import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startTime,
        Long cohortId,
        SessionStatus status
) {
    public static SessionResponse from(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getStartTime(),
                session.getCohort().getId(),
                session.getStatus()
        );
    }
}
