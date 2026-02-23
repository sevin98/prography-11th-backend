package com.prography11thbackend.api.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AttendanceCheckRequest(
        @NotBlank(message = "QR 해시 값은 필수입니다")
        String hashValue,
        @NotNull(message = "회원 ID는 필수입니다")
        Long memberId
) {
}
