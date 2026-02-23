package com.prography11thbackend.api.attendance.dto;

public record AttendanceCreateRequest(
        Long sessionId,
        Long memberId,
        String status,
        Integer lateMinutes,
        String reason
) {
}
