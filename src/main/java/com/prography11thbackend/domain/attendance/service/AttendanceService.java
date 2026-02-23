package com.prography11thbackend.domain.attendance.service;

import com.prography11thbackend.api.attendance.dto.AttendanceSummaryResponse;
import com.prography11thbackend.domain.attendance.entity.Attendance;

import java.util.List;

public interface AttendanceService {

    Attendance checkAttendanceByQR(String qrHashValue, Long memberId);

    List<Attendance> getMyAttendances(Long memberId);

    Attendance createAttendance(Long sessionId, Long memberId, String status, Integer lateMinutes, String reason);

    Attendance updateAttendance(Long attendanceId, String newStatus, Integer lateMinutes, String reason);

    List<Attendance> getAttendancesBySession(Long sessionId);

    List<Attendance> getAttendancesByMember(Long memberId);

    AttendanceSummaryResponse getAttendanceSummaryByMember(Long memberId);

    List<com.prography11thbackend.api.attendance.dto.MemberAttendanceSummaryResponse> getAttendanceSummaryBySession(Long sessionId);

    com.prography11thbackend.api.attendance.dto.MemberAttendanceDetailResponse getMemberAttendanceDetail(Long memberId);

    com.prography11thbackend.api.attendance.dto.SessionAttendanceListResponse getSessionAttendances(Long sessionId);
}
