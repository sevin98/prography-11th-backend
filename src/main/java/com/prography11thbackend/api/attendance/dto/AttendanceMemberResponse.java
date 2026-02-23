package com.prography11thbackend.api.attendance.dto;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record AttendanceMemberResponse(
        Long id,
        Long sessionId,
        String sessionTitle,
        AttendanceStatus status,
        Integer lateMinutes,
        Integer penaltyAmount,
        String reason,
        Instant checkedInAt,
        Instant createdAt
) {
    public static AttendanceMemberResponse from(Attendance attendance) {
        if (attendance.getSession() == null) {
            throw new IllegalStateException("Attendance must have a session");
        }
        
        LocalDateTime sessionStartTime = attendance.getSession().getStartTime();
        LocalDateTime checkedAt = attendance.getCheckedAt();
        
        Integer lateMinutes = null;
        if (checkedAt != null && sessionStartTime != null && checkedAt.isAfter(sessionStartTime)) {
            lateMinutes = (int) java.time.Duration.between(sessionStartTime, checkedAt).toMinutes();
        }
        
        return new AttendanceMemberResponse(
                attendance.getId(),
                attendance.getSession().getId(),
                attendance.getSession().getTitle(),
                attendance.getStatus(),
                lateMinutes,
                attendance.getPenalty(),
                attendance.getReason(),
                checkedAt != null ? checkedAt.toInstant(ZoneOffset.UTC) : null,
                attendance.getCreatedAt() != null ? attendance.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
