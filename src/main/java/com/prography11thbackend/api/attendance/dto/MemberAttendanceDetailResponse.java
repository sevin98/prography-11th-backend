package com.prography11thbackend.api.attendance.dto;

import java.util.List;

public record MemberAttendanceDetailResponse(
        Long memberId,
        String memberName,
        Integer generation,
        String partName,
        String teamName,
        Integer deposit,
        Integer excuseCount,
        List<AttendanceAdminResponse> attendances
) {
}
