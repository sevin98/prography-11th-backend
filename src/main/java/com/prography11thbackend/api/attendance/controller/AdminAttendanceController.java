package com.prography11thbackend.api.attendance.controller;

import com.prography11thbackend.api.attendance.dto.AttendanceCreateRequest;
import com.prography11thbackend.api.attendance.dto.AttendanceResponse;
import com.prography11thbackend.api.attendance.dto.AttendanceUpdateRequest;
import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/attendances")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> createAttendance(@RequestBody AttendanceCreateRequest request) {
        Attendance attendance = attendanceService.createAttendance(
                request.memberId(),
                request.sessionId(),
                request.status()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceResponse.from(attendance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> updateAttendance(@PathVariable Long id, @RequestBody AttendanceUpdateRequest request) {
        Attendance attendance = attendanceService.updateAttendance(id, request.status());
        return ResponseEntity.ok(AttendanceResponse.from(attendance));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendancesBySession(@PathVariable Long sessionId) {
        List<AttendanceResponse> attendances = attendanceService.getAttendancesBySession(sessionId).stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendancesByMember(@PathVariable Long memberId) {
        List<AttendanceResponse> attendances = attendanceService.getAttendancesByMember(memberId).stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/sessions/{sessionId}/summary")
    public ResponseEntity<com.prography11thbackend.api.attendance.dto.AttendanceSummaryResponse> getAttendanceSummaryBySession(@PathVariable Long sessionId) {
        com.prography11thbackend.api.attendance.dto.AttendanceSummaryResponse summary = attendanceService.getAttendanceSummaryBySession(sessionId);
        return ResponseEntity.ok(summary);
    }
}
