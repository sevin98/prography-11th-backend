package com.prography11thbackend.api.attendance.controller;

import com.prography11thbackend.api.attendance.dto.AttendanceCheckRequest;
import com.prography11thbackend.api.attendance.dto.AttendanceResponse;
import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> checkAttendance(@RequestBody AttendanceCheckRequest request) {
        Attendance attendance = attendanceService.checkAttendanceByQR(
                request.qrHashValue(),
                request.memberId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceResponse.from(attendance));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getMyAttendances(@RequestParam Long memberId) {
        List<AttendanceResponse> attendances = attendanceService.getMyAttendances(memberId).stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(attendances);
    }
}
