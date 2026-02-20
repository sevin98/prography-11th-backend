package com.prography11thbackend.api.deposit.dto;

import com.prography11thbackend.domain.deposit.entity.Deposit;

public record DepositResponse(
        Long id,
        Long memberId,
        Integer balance
) {
    public static DepositResponse from(Deposit deposit) {
        return new DepositResponse(
                deposit.getId(),
                deposit.getMember().getId(),
                deposit.getBalance()
        );
    }
}
