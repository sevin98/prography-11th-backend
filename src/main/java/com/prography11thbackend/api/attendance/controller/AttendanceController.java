package com.prography11thbackend.api.attendance.controller;

import com.prography11thbackend.api.attendance.dto.AttendanceCheckRequest;
import com.prography11thbackend.api.attendance.dto.AttendanceMemberResponse;
import com.prography11thbackend.api.attendance.dto.AttendanceResponse;
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
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkAttendance(@Valid @RequestBody AttendanceCheckRequest request) {
        Attendance attendance = attendanceService.checkAttendanceByQR(
                request.hashValue(),
                request.memberId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AttendanceResponse.from(attendance)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceMemberResponse>>> getMyAttendances(@RequestParam Long memberId) {
        List<AttendanceMemberResponse> attendances = attendanceService.getMyAttendances(memberId).stream()
                .map(AttendanceMemberResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }
}
