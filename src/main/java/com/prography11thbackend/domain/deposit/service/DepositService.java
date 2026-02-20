package com.prography11thbackend.domain.deposit.service;

import com.prography11thbackend.domain.deposit.entity.Deposit;
import com.prography11thbackend.domain.deposit.entity.DepositHistory;

import java.util.List;

public interface DepositService {

    Deposit createInitialDeposit(Long memberId);

    void deductPenalty(Long memberId, Integer penalty, String description);

    void refundPenalty(Long memberId, Integer refundAmount, String description);

    Deposit getDepositByMemberId(Long memberId);

    java.util.Optional<Deposit> findDepositByMemberId(Long memberId);

    Deposit getDepositByCohortMemberId(Long cohortMemberId);

    List<DepositHistory> getDepositHistory(Long depositId);
}
