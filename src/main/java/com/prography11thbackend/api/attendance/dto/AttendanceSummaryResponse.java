package com.prography11thbackend.api.attendance.dto;

public record AttendanceSummaryResponse(
        Long memberId,
        Integer present,
        Integer absent,
        Integer late,
        Integer excused,
        Integer totalPenalty,
        Integer deposit
) {
}
