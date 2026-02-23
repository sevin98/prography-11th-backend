package com.prography11thbackend.api.attendance.dto;

public record MemberAttendanceSummaryResponse(
        Long memberId,
        String memberName,
        Integer present,
        Integer absent,
        Integer late,
        Integer excused,
        Integer totalPenalty,
        Integer deposit
) {
}
