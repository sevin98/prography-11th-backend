package com.prography11thbackend.api.deposit.dto;

import com.prography11thbackend.domain.deposit.entity.DepositHistory;
import com.prography11thbackend.domain.deposit.entity.DepositHistoryType;

import java.time.Instant;
import java.time.ZoneOffset;

public record DepositHistoryResponse(
        Long id,
        Long cohortMemberId,
        DepositHistoryType type,
        Integer amount,
        Integer balanceAfter,
        Long attendanceId,
        String description,
        Instant createdAt
) {
    public static DepositHistoryResponse from(DepositHistory history, Long cohortMemberId, Long attendanceId) {
        return new DepositHistoryResponse(
                history.getId(),
                cohortMemberId,
                history.getType(),
                history.getAmount(),
                history.getBalanceAfter(),
                attendanceId,
                history.getDescription(),
                history.getCreatedAt() != null ? history.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
