package com.prography11thbackend.api.attendance.dto;

public record AttendanceUpdateRequest(
        String status,
        Integer lateMinutes,
        String reason
) {
}
