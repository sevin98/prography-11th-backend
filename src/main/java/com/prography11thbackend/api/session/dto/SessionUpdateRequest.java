package com.prography11thbackend.api.session.dto;

import com.prography11thbackend.domain.session.entity.SessionStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionUpdateRequest(
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status
) {
}
