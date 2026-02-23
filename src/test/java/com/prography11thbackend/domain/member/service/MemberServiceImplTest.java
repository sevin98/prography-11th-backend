package com.prography11thbackend.domain.member.service;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.entity.Part;
import com.prography11thbackend.domain.cohort.entity.Team;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
import com.prography11thbackend.domain.cohort.service.CohortService;
import com.prography11thbackend.domain.deposit.service.DepositService;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CohortService cohortService;

    @Mock
    private DepositService depositService;

    @Mock
    private CohortMemberRepository cohortMemberRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;
    private Cohort cohort;
    private Team team;

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
        cohort = Cohort.builder()
                .number(11)
                .build();
        setId(cohort, 1L);

        team = Team.builder()
                .name("Team A")
                .cohort(cohort)
                .build();
        setId(team, 1L);

        member = Member.builder()
                .loginId("testuser")
                .passwordHash("encoded")
                .name("테스트유저")
                .phone("010-1234-5678")
                .role(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();
        setId(member, 1L);
    }

    @Test
    @DisplayName("회원 등록 성공")
    void register_Success() {
        // given
        String loginId = "newuser";
        String rawPassword = "password123";
        String name = "새유저";
        String phone = "010-9999-9999";
        Long cohortId = 1L;
        Long partId = 6L; // SERVER (ordinal 0 + 6 = 6)
        Long teamId = 1L;

        when(memberRepository.existsByLoginId(loginId)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn("encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            Member saved = Member.builder()
                    .loginId(member.getLoginId())
                    .passwordHash(member.getPasswordHash())
                    .name(member.getName())
                    .phone(member.getPhone())
                    .role(member.getRole())
                    .status(member.getStatus())
                    .build();
            setId(saved, 1L);
            return saved;
        });

        // when
        Member result = memberService.register(loginId, rawPassword, name, phone, cohortId, partId, teamId);

        // then
        assertThat(result.getLoginId()).isEqualTo(loginId);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getRole()).isEqualTo(MemberRole.MEMBER);
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        verify(memberRepository, times(1)).existsByLoginId(loginId);
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(cohortService, times(1)).createCohortMember(anyLong(), eq(cohortId), eq(partId), eq(teamId));
        verify(depositService, times(1)).createInitialDeposit(anyLong());
    }

    @Test
    @DisplayName("회원 등록 실패 - 중복된 loginId")
    void register_Fail_DuplicateLoginId() {
        // given
        String loginId = "existinguser";
        String rawPassword = "password123";
        String name = "유저";
        String phone = "010-1234-5678";
        Long cohortId = 1L;
        Long partId = 6L;
        Long teamId = 1L;

        when(memberRepository.existsByLoginId(loginId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(loginId, rawPassword, name, phone, cohortId, partId, teamId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    @DisplayName("회원 조회 성공")
    void getMember_Success() {
        // given
        Long memberId = 1L;

        when(memberRepository.findActiveById(memberId)).thenReturn(Optional.of(member));

        // when
        Optional<Member> result = memberService.getMember(memberId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(member);
        verify(memberRepository, times(1)).findActiveById(memberId);
    }

    @Test
    @DisplayName("회원 수정 성공")
    void updateMember_Success() {
        // given
        Long memberId = 1L;
        String newName = "수정된이름";
        String newPhone = "010-8888-8888";
        Long cohortId = 1L;
        Long partId = 6L;
        Long teamId = 1L;

        CohortMember existingCohortMember = CohortMember.builder()
                .member(member)
                .cohort(cohort)
                .part(Part.SERVER)
                .team(team)
                .build();
        setId(existingCohortMember, 1L);

        when(memberRepository.findActiveById(memberId)).thenReturn(Optional.of(member));
        when(cohortMemberRepository.findByMemberIdAndCohortId(memberId, cohortId))
                .thenReturn(Optional.of(existingCohortMember));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // when
        Member result = memberService.updateMember(memberId, newName, newPhone, cohortId, partId, teamId);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getPhone()).isEqualTo(newPhone);
        verify(memberRepository, times(1)).findActiveById(memberId);
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdraw_Success() {
        // given
        Long memberId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member result = memberService.withdraw(memberId);

        // then
        assertThat(result.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이미 탈퇴한 회원")
    void withdraw_Fail_AlreadyWithdrawn() {
        // given
        Long memberId = 1L;
        Member withdrawnMember = Member.builder()
                .loginId("testuser")
                .passwordHash("encoded")
                .name("테스트유저")
                .phone(null)
                .role(null)
                .status(MemberStatus.WITHDRAWN)
                .build();
        setId(withdrawnMember, 1L);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(withdrawnMember));

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 회원 없음")
    void withdraw_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }
}
