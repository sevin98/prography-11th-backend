package com.prography11thbackend.api.deposit.controller;

import com.prography11thbackend.api.deposit.dto.DepositHistoryResponse;
import com.prography11thbackend.domain.deposit.entity.Deposit;
import com.prography11thbackend.domain.deposit.entity.DepositHistory;
import com.prography11thbackend.domain.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/cohort-members")
@RequiredArgsConstructor
public class AdminDepositController {

    private final DepositService depositService;

    @GetMapping("/{cohortMemberId}/deposits")
    public ResponseEntity<List<DepositHistoryResponse>> getDepositHistory(@PathVariable Long cohortMemberId) {
        Deposit deposit = depositService.getDepositByCohortMemberId(cohortMemberId);
        List<DepositHistory> histories = depositService.getDepositHistory(deposit.getId());
        List<DepositHistoryResponse> responses = histories.stream()
                .map(DepositHistoryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
