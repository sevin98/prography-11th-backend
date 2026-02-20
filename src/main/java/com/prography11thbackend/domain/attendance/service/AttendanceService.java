package com.prography11thbackend.domain.attendance.service;

import com.prography11thbackend.api.attendance.dto.AttendanceSummaryResponse;
import com.prography11thbackend.domain.attendance.entity.Attendance;

import java.util.List;

public interface AttendanceService {

    Attendance checkAttendanceByQR(String qrHashValue, Long memberId);

    List<Attendance> getMyAttendances(Long memberId);

    Attendance createAttendance(Long memberId, Long sessionId, String status);

    Attendance updateAttendance(Long attendanceId, String newStatus);

    List<Attendance> getAttendancesBySession(Long sessionId);

    List<Attendance> getAttendancesByMember(Long memberId);

    AttendanceSummaryResponse getAttendanceSummaryByMember(Long memberId);

    AttendanceSummaryResponse getAttendanceSummaryBySession(Long sessionId);
}
