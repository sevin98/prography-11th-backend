package com.prography11thbackend.api.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionCreateRequest(
        @NotBlank(message = "제목은 필수입니다")
        String title,
        @NotNull(message = "날짜는 필수입니다")
        LocalDate date,
        @NotNull(message = "시간은 필수입니다")
        LocalTime time,
        String location
) {
}
