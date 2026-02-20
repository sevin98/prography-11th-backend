package com.prography11thbackend.api.attendance.dto;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;

import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long memberId,
        Long sessionId,
        AttendanceStatus status,
        Integer penalty,
        LocalDateTime checkedAt
) {
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getMember().getId(),
                attendance.getSession().getId(),
                attendance.getStatus(),
                attendance.getPenalty(),
                attendance.getCheckedAt()
        );
    }
}
