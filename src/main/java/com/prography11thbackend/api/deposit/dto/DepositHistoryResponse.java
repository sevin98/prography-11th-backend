package com.prography11thbackend.api.deposit.dto;

import com.prography11thbackend.domain.deposit.entity.DepositHistory;
import com.prography11thbackend.domain.deposit.entity.DepositHistoryType;

import java.time.LocalDateTime;

public record DepositHistoryResponse(
        Long id,
        DepositHistoryType type,
        Integer amount,
        Integer balanceAfter,
        String description,
        LocalDateTime createdAt
) {
    public static DepositHistoryResponse from(DepositHistory history) {
        return new DepositHistoryResponse(
                history.getId(),
                history.getType(),
                history.getAmount(),
                history.getBalanceAfter(),
                history.getDescription(),
                history.getCreatedAt()
        );
    }
}
