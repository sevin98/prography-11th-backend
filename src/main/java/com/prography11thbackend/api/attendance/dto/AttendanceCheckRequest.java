package com.prography11thbackend.api.attendance.dto;

public record AttendanceCheckRequest(
        String qrHashValue,
        Long memberId
) {
}
