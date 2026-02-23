package com.prography11thbackend.api.session.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionCreateRequest(
        String title,
        LocalDate date,
        LocalTime time,
        String location
) {
}
