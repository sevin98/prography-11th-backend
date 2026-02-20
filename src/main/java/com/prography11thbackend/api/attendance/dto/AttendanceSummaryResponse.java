package com.prography11thbackend.api.attendance.dto;

public record AttendanceSummaryResponse(
        Integer totalSessions,
        Integer presentCount,
        Integer lateCount,
        Integer absentCount,
        Integer excusedCount,
        Integer totalPenalty
) {
}
