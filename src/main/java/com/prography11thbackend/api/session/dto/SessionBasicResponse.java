package com.prography11thbackend.api.session.dto;

import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public record SessionBasicResponse(
        Long id,
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static SessionBasicResponse from(Session session) {
        return new SessionBasicResponse(
                session.getId(),
                session.getTitle(),
                session.getStartTime() != null ? session.getStartTime().toLocalDate() : null,
                session.getStartTime() != null ? session.getStartTime().toLocalTime() : null,
                session.getLocation(),
                session.getStatus(),
                session.getCreatedAt() != null ? session.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                session.getUpdatedAt() != null ? session.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
