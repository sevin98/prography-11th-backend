package com.prography11thbackend.api.attendance.dto;

public record AttendanceCreateRequest(
        Long memberId,
        Long sessionId,
        String status
) {
}
