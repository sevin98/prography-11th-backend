package com.prography11thbackend.api.session.dto;

import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public record SessionAdminResponse(
        Long id,
        Long cohortId,
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status,
        AttendanceSummary attendanceSummary,
        Boolean qrActive,
        Instant createdAt,
        Instant updatedAt
) {
    public record AttendanceSummary(
            Integer present,
            Integer absent,
            Integer late,
            Integer excused,
            Integer total
    ) {
    }

    public static SessionAdminResponse from(Session session, AttendanceSummary attendanceSummary, Boolean qrActive) {
        return new SessionAdminResponse(
                session.getId(),
                session.getCohort().getId(),
                session.getTitle(),
                session.getStartTime() != null ? session.getStartTime().toLocalDate() : null,
                session.getStartTime() != null ? session.getStartTime().toLocalTime() : null,
                session.getLocation(),
                session.getStatus(),
                attendanceSummary,
                qrActive,
                session.getCreatedAt() != null ? session.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                session.getUpdatedAt() != null ? session.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
