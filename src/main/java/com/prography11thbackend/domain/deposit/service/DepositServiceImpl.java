package com.prography11thbackend.domain.deposit.service;

import com.prography11thbackend.domain.deposit.entity.Deposit;
import com.prography11thbackend.domain.deposit.entity.DepositHistory;
import com.prography11thbackend.domain.deposit.entity.DepositHistoryType;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.deposit.repository.DepositHistoryRepository;
import com.prography11thbackend.domain.deposit.repository.DepositRepository;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepositServiceImpl implements DepositService {

    private static final Integer INITIAL_DEPOSIT_AMOUNT = 100_000;

    private final DepositRepository depositRepository;
    private final DepositHistoryRepository depositHistoryRepository;
    private final MemberRepository memberRepository;
    private final CohortMemberRepository cohortMemberRepository;

    @Override
    public Deposit createInitialDeposit(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Deposit deposit = Deposit.builder()
                .member(member)
                .balance(INITIAL_DEPOSIT_AMOUNT)
                .build();

        Deposit savedDeposit = depositRepository.save(deposit);

        // 초기 보증금 이력 기록
        DepositHistory history = DepositHistory.builder()
                .deposit(savedDeposit)
                .type(DepositHistoryType.INITIAL)
                .amount(INITIAL_DEPOSIT_AMOUNT)
                .balanceAfter(INITIAL_DEPOSIT_AMOUNT)
                .description("초기 보증금")
                .build();
        depositHistoryRepository.save(history);

        return savedDeposit;
    }

    @Override
    public void deductPenalty(Long memberId, Integer penalty, String description) {
        Deposit deposit = depositRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_NOT_FOUND));

        if (deposit.getBalance() < penalty) {
            throw new BusinessException(ErrorCode.DEPOSIT_INSUFFICIENT);
        }

        deposit.deduct(penalty);

        DepositHistory history = DepositHistory.builder()
                .deposit(deposit)
                .type(DepositHistoryType.PENALTY)
                .amount(penalty)
                .balanceAfter(deposit.getBalance())
                .description(description)
                .build();
        depositHistoryRepository.save(history);
    }

    @Override
    public void refundPenalty(Long memberId, Integer refundAmount, String description) {
        Deposit deposit = depositRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_NOT_FOUND));

        deposit.refund(refundAmount);

        DepositHistory history = DepositHistory.builder()
                .deposit(deposit)
                .type(DepositHistoryType.REFUND)
                .amount(refundAmount)
                .balanceAfter(deposit.getBalance())
                .description(description)
                .build();
        depositHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public Deposit getDepositByMemberId(Long memberId) {
        return depositRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Deposit> findDepositByMemberId(Long memberId) {
        return depositRepository.findByMemberId(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public Deposit getDepositByCohortMemberId(Long cohortMemberId) {
        com.prography11thbackend.domain.cohort.entity.CohortMember cohortMember = 
                cohortMemberRepository.findById(cohortMemberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
        return getDepositByMemberId(cohortMember.getMember().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositHistory> getDepositHistory(Long depositId) {
        return depositHistoryRepository.findByDepositIdOrderByCreatedAtDesc(depositId);
    }
}
