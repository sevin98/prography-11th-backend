package com.prography11thbackend.domain.cohort.service;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.entity.Part;
import com.prography11thbackend.domain.cohort.entity.Team;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CohortService 단위 테스트")
class CohortServiceImplTest {

    @Mock
    private CohortRepository cohortRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CohortMemberRepository cohortMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CohortServiceImpl cohortService;

    private Cohort cohort;
    private Member member;
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

        member = Member.builder()
                .loginId("testuser")
                .passwordHash("encoded")
                .name("테스트유저")
                .phone(null)
                .role(null)
                .status(null)
                .build();
        setId(member, 1L);

        team = Team.builder()
                .name("Team A")
                .cohort(cohort)
                .build();
        setId(team, 1L);
    }

    @Test
    @DisplayName("기수 목록 조회 성공")
    void getAllCohorts_Success() {
        // given
        Cohort cohort1 = Cohort.builder().number(10).build();
        setId(cohort1, 1L);
        Cohort cohort2 = Cohort.builder().number(11).build();
        setId(cohort2, 2L);

        when(cohortRepository.findAll()).thenReturn(List.of(cohort1, cohort2));

        // when
        List<Cohort> result = cohortService.getAllCohorts();

        // then
        assertThat(result).hasSize(2);
        verify(cohortRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("기수 조회 성공 - ID로")
    void getCohortById_Success() {
        // given
        Long cohortId = 1L;

        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));

        // when
        Optional<Cohort> result = cohortService.getCohortById(cohortId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(cohort);
        verify(cohortRepository, times(1)).findById(cohortId);
    }

    @Test
    @DisplayName("기수 회원 생성 성공")
    void createCohortMember_Success() {
        // given
        Long memberId = 1L;
        Long cohortId = 1L;
        Long partId = 6L; // SERVER (ordinal 0 + 6 = 6)
        Long teamId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(cohortMemberRepository.save(any(CohortMember.class))).thenAnswer(invocation -> {
            CohortMember cohortMember = invocation.getArgument(0);
            CohortMember saved = CohortMember.builder()
                    .member(cohortMember.getMember())
                    .cohort(cohortMember.getCohort())
                    .part(cohortMember.getPart())
                    .team(cohortMember.getTeam())
                    .build();
            setId(saved, 1L);
            return saved;
        });

        // when
        CohortMember result = cohortService.createCohortMember(memberId, cohortId, partId, teamId);

        // then
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getCohort()).isEqualTo(cohort);
        assertThat(result.getPart()).isEqualTo(Part.SERVER);
        assertThat(result.getTeam()).isEqualTo(team);
        verify(memberRepository, times(1)).findById(memberId);
        verify(cohortRepository, times(1)).findById(cohortId);
        verify(teamRepository, times(1)).findById(teamId);
        verify(cohortMemberRepository, times(1)).save(any(CohortMember.class));
    }

    @Test
    @DisplayName("기수 회원 생성 실패 - 회원 없음")
    void createCohortMember_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;
        Long cohortId = 1L;
        Long partId = 6L;
        Long teamId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cohortService.createCohortMember(memberId, cohortId, partId, teamId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("기수 회원 생성 실패 - 기수 없음")
    void createCohortMember_Fail_CohortNotFound() {
        // given
        Long memberId = 1L;
        Long cohortId = 999L;
        Long partId = 6L;
        Long teamId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cohortRepository.findById(cohortId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cohortService.createCohortMember(memberId, cohortId, partId, teamId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COHORT_NOT_FOUND);
    }

    @Test
    @DisplayName("기수 회원 생성 실패 - 파트 없음")
    void createCohortMember_Fail_PartNotFound() {
        // given
        Long memberId = 1L;
        Long cohortId = 1L;
        Long partId = 999L; // 잘못된 partId
        Long teamId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));

        // when & then
        assertThatThrownBy(() -> cohortService.createCohortMember(memberId, cohortId, partId, teamId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PART_NOT_FOUND);
    }

    @Test
    @DisplayName("기수 회원 생성 실패 - 팀 없음")
    void createCohortMember_Fail_TeamNotFound() {
        // given
        Long memberId = 1L;
        Long cohortId = 1L;
        Long partId = 6L; // 유효한 partId (6=SERVER)
        Long teamId = 999L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cohortService.createCohortMember(memberId, cohortId, partId, teamId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);
    }
}
