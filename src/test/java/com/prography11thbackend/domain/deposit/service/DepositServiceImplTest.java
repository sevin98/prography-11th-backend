package com.prography11thbackend.domain.deposit.service;

import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.deposit.entity.Deposit;
import com.prography11thbackend.domain.deposit.entity.DepositHistory;
import com.prography11thbackend.domain.deposit.entity.DepositHistoryType;
import com.prography11thbackend.domain.deposit.repository.DepositHistoryRepository;
import com.prography11thbackend.domain.deposit.repository.DepositRepository;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepositService 단위 테스트")
class DepositServiceImplTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private DepositHistoryRepository depositHistoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CohortMemberRepository cohortMemberRepository;

    @InjectMocks
    private DepositServiceImpl depositService;

    private Member member;
    private Deposit deposit;
    private CohortMember cohortMember;

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .loginId("testuser")
                .passwordHash("encoded")
                .name("테스트유저")
                .phone(null)
                .role(null)
                .status(null)
                .build();
        setId(member, 1L);

        deposit = Deposit.builder()
                .member(member)
                .balance(100000)
                .build();
        setId(deposit, 1L);

        cohortMember = CohortMember.builder()
                .member(member)
                .cohort(null)
                .part(null)
                .team(null)
                .build();
        setId(cohortMember, 1L);
    }

    @Test
    @DisplayName("초기 보증금 생성 성공")
    void createInitialDeposit_Success() {
        // given
        Long memberId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(depositRepository.save(any(Deposit.class))).thenAnswer(invocation -> {
            Deposit deposit = invocation.getArgument(0);
            Deposit saved = Deposit.builder()
                    .member(deposit.getMember())
                    .balance(deposit.getBalance())
                    .build();
            setId(saved, 1L);
            return saved;
        });
        when(depositHistoryRepository.save(any(DepositHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Deposit result = depositService.createInitialDeposit(memberId);

        // then
        assertThat(result.getBalance()).isEqualTo(100000);
        assertThat(result.getMember()).isEqualTo(member);
        verify(depositRepository, times(1)).save(any(Deposit.class));
        verify(depositHistoryRepository, times(1)).save(any(DepositHistory.class));
    }

    @Test
    @DisplayName("초기 보증금 생성 실패 - 회원 없음")
    void createInitialDeposit_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> depositService.createInitialDeposit(memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("패널티 차감 성공")
    void deductPenalty_Success() {
        // given
        Long memberId = 1L;
        Integer penalty = 5000;
        String description = "지각 패널티";

        when(depositRepository.findByMemberId(memberId)).thenReturn(Optional.of(deposit));
        when(depositHistoryRepository.save(any(DepositHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        depositService.deductPenalty(memberId, penalty, description);

        // then
        assertThat(deposit.getBalance()).isEqualTo(95000);
        verify(depositRepository, times(1)).findByMemberId(memberId);
        verify(depositHistoryRepository, times(1)).save(argThat(history ->
                history.getType() == DepositHistoryType.PENALTY &&
                history.getAmount().equals(penalty) &&
                history.getDescription().equals(description)
        ));
    }

    @Test
    @DisplayName("패널티 차감 실패 - 잔액 부족")
    void deductPenalty_Fail_InsufficientBalance() {
        // given
        Long memberId = 1L;
        Integer penalty = 150000; // 잔액보다 큰 패널티
        String description = "패널티";

        deposit = Deposit.builder()
                .member(member)
                .balance(100000)
                .build();
        setId(deposit, 1L);

        when(depositRepository.findByMemberId(memberId)).thenReturn(Optional.of(deposit));

        // when & then
        assertThatThrownBy(() -> depositService.deductPenalty(memberId, penalty, description))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPOSIT_INSUFFICIENT);
    }

    @Test
    @DisplayName("패널티 환급 성공")
    void refundPenalty_Success() {
        // given
        Long memberId = 1L;
        Integer refundAmount = 5000;
        String description = "출결 수정 환급";

        deposit = Deposit.builder()
                .member(member)
                .balance(50000)
                .build();
        setId(deposit, 1L);

        when(depositRepository.findByMemberId(memberId)).thenReturn(Optional.of(deposit));
        when(depositHistoryRepository.save(any(DepositHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        depositService.refundPenalty(memberId, refundAmount, description);

        // then
        assertThat(deposit.getBalance()).isEqualTo(55000);
        verify(depositRepository, times(1)).findByMemberId(memberId);
        verify(depositHistoryRepository, times(1)).save(argThat(history ->
                history.getType() == DepositHistoryType.REFUND &&
                history.getAmount().equals(refundAmount) &&
                history.getDescription().equals(description)
        ));
    }

    @Test
    @DisplayName("보증금 조회 성공 - 회원 ID로")
    void getDepositByMemberId_Success() {
        // given
        Long memberId = 1L;

        when(depositRepository.findByMemberId(memberId)).thenReturn(Optional.of(deposit));

        // when
        Deposit result = depositService.getDepositByMemberId(memberId);

        // then
        assertThat(result).isEqualTo(deposit);
        verify(depositRepository, times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("보증금 조회 실패 - 보증금 없음")
    void getDepositByMemberId_Fail_NotFound() {
        // given
        Long memberId = 999L;

        when(depositRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> depositService.getDepositByMemberId(memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPOSIT_NOT_FOUND);
    }

    @Test
    @DisplayName("보증금 조회 성공 - 기수 회원 ID로")
    void getDepositByCohortMemberId_Success() {
        // given
        Long cohortMemberId = 1L;
        Long memberId = 1L;

        when(cohortMemberRepository.findById(cohortMemberId)).thenReturn(Optional.of(cohortMember));
        when(depositRepository.findByMemberId(memberId)).thenReturn(Optional.of(deposit));

        // when
        Deposit result = depositService.getDepositByCohortMemberId(cohortMemberId);

        // then
        assertThat(result).isEqualTo(deposit);
        verify(cohortMemberRepository, times(1)).findById(cohortMemberId);
        verify(depositRepository, times(1)).findByMemberId(memberId);
    }
}
