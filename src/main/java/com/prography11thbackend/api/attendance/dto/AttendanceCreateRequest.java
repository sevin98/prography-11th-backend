package com.prography11thbackend.api.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AttendanceCreateRequest(
        @NotNull(message = "세션 ID는 필수입니다")
        Long sessionId,
        @NotNull(message = "회원 ID는 필수입니다")
        Long memberId,
        @NotNull(message = "출결 상태는 필수입니다")
        String status,
        @PositiveOrZero(message = "지각 시간은 0 이상이어야 합니다")
        Integer lateMinutes,
        String reason
) {
}
