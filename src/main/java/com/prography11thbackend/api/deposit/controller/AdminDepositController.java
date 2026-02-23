package com.prography11thbackend.api.deposit.controller;

import com.prography11thbackend.api.deposit.dto.DepositHistoryResponse;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.deposit.entity.Deposit;
import com.prography11thbackend.domain.deposit.entity.DepositHistory;
import com.prography11thbackend.domain.deposit.service.DepositService;
import com.prography11thbackend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/cohort-members")
@RequiredArgsConstructor
public class AdminDepositController {

    private final DepositService depositService;
    private final CohortMemberRepository cohortMemberRepository;

    @GetMapping("/{cohortMemberId}/deposits")
    public ResponseEntity<ApiResponse<List<DepositHistoryResponse>>> getDepositHistory(@PathVariable Long cohortMemberId) {
        Deposit deposit = depositService.getDepositByCohortMemberId(cohortMemberId);
        List<DepositHistory> histories = depositService.getDepositHistory(deposit.getId());
        
        // description에서 attendanceId 추출 시도 (예: "출결 등록 - ABSENT 패널티 10000원" 또는 "출결 ID: 123")
        Pattern attendanceIdPattern = Pattern.compile("출결\\s*ID[\\s:]*?(\\d+)", Pattern.CASE_INSENSITIVE);
        
        List<DepositHistoryResponse> responses = histories.stream()
                .map(history -> {
                    Long attendanceId = null;
                    if (history.getDescription() != null) {
                        Matcher matcher = attendanceIdPattern.matcher(history.getDescription());
                        if (matcher.find()) {
                            try {
                                attendanceId = Long.parseLong(matcher.group(1));
                            } catch (NumberFormatException e) {
                                // 파싱 실패 시 null 유지
                            }
                        }
                    }
                    return DepositHistoryResponse.from(history, cohortMemberId, attendanceId);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
