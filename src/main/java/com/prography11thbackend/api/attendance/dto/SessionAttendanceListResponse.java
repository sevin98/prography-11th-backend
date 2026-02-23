package com.prography11thbackend.api.attendance.dto;

import java.util.List;

public record SessionAttendanceListResponse(
        Long sessionId,
        String sessionTitle,
        List<AttendanceAdminResponse> attendances
) {
}
