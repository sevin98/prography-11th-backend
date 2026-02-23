package com.prography11thbackend.api.attendance.dto;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record AttendanceAdminResponse(
        Long id,
        Long sessionId,
        Long memberId,
        AttendanceStatus status,
        Integer lateMinutes,
        Integer penaltyAmount,
        String reason,
        Instant checkedInAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static AttendanceAdminResponse from(Attendance attendance) {
        LocalDateTime sessionStartTime = attendance.getSession().getStartTime();
        LocalDateTime checkedAt = attendance.getCheckedAt();
        
        Integer lateMinutes = null;
        if (checkedAt != null && sessionStartTime != null && checkedAt.isAfter(sessionStartTime)) {
            lateMinutes = (int) java.time.Duration.between(sessionStartTime, checkedAt).toMinutes();
        }
        
        return new AttendanceAdminResponse(
                attendance.getId(),
                attendance.getSession().getId(),
                attendance.getMember().getId(),
                attendance.getStatus(),
                lateMinutes,
                attendance.getPenalty(),
                attendance.getReason(),
                checkedAt != null ? checkedAt.atZone(ZoneId.systemDefault()).toInstant() : null,
                attendance.getCreatedAt() != null ? attendance.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                attendance.getUpdatedAt() != null ? attendance.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
