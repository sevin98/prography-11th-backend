package com.prography11thbackend.api.member.controller;

import com.prography11thbackend.api.attendance.dto.AttendanceSummaryResponse;
import com.prography11thbackend.api.member.dto.MemberBasicResponse;
import com.prography11thbackend.domain.attendance.service.AttendanceService;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.service.MemberService;
import com.prography11thbackend.global.common.ApiResponse;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AttendanceService attendanceService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberBasicResponse>> getMember(@PathVariable Long id) {
        Member member = memberService.getMember(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(MemberBasicResponse.from(member)));
    }

    @GetMapping("/{id}/attendance-summary")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getAttendanceSummary(@PathVariable Long id) {
        AttendanceSummaryResponse summary = attendanceService.getAttendanceSummaryByMember(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
