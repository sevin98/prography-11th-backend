package com.prography11thbackend.api.attendance.controller;

import com.prography11thbackend.api.attendance.dto.AttendanceCreateRequest;
import com.prography11thbackend.api.attendance.dto.AttendanceResponse;
import com.prography11thbackend.api.attendance.dto.AttendanceUpdateRequest;
import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.service.AttendanceService;
import com.prography11thbackend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/attendances")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse>> createAttendance(@Valid @RequestBody AttendanceCreateRequest request) {
        Attendance attendance = attendanceService.createAttendance(
                request.sessionId(),
                request.memberId(),
                request.status(),
                request.lateMinutes(),
                request.reason()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse.from(attendance)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse>> updateAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceUpdateRequest request) {
        Attendance attendance = attendanceService.updateAttendance(id, request.status(), request.lateMinutes(), request.reason());
        return ResponseEntity.ok(ApiResponse.success(com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse.from(attendance)));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<com.prography11thbackend.api.attendance.dto.SessionAttendanceListResponse>> getAttendancesBySession(@PathVariable Long sessionId) {
        com.prography11thbackend.api.attendance.dto.SessionAttendanceListResponse response = attendanceService.getSessionAttendances(sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<com.prography11thbackend.api.attendance.dto.MemberAttendanceDetailResponse>> getAttendancesByMember(@PathVariable Long memberId) {
        com.prography11thbackend.api.attendance.dto.MemberAttendanceDetailResponse response = attendanceService.getMemberAttendanceDetail(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sessions/{sessionId}/summary")
    public ResponseEntity<ApiResponse<List<com.prography11thbackend.api.attendance.dto.MemberAttendanceSummaryResponse>>> getAttendanceSummaryBySession(@PathVariable Long sessionId) {
        List<com.prography11thbackend.api.attendance.dto.MemberAttendanceSummaryResponse> summary = attendanceService.getAttendanceSummaryBySession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
