package com.prography11thbackend.api.deposit;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;
import com.prography11thbackend.domain.attendance.repository.AttendanceRepository;
import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.entity.Part;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.deposit.service.DepositService;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;
import com.prography11thbackend.domain.session.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin Deposit API 테스트")
class AdminDepositControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private CohortMemberRepository cohortMemberRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DepositService depositService;

    private Cohort cohort11;
    private Member testMember;
    private CohortMember cohortMember;
    private Session testSession;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        testMember = memberRepository.findByLoginId("deposit_test_user")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .loginId("deposit_test_user")
                            .passwordHash("$2a$12$encoded")
                            .name("보증금테스트유저")
                            .phone("010-3333-4444")
                            .status(MemberStatus.ACTIVE)
                            .role(MemberRole.MEMBER)
                            .build();
                    return memberRepository.save(member);
                });

        depositService.createInitialDeposit(testMember.getId());

        cohortMember = cohortMemberRepository.findByMemberIdAndCohortId(testMember.getId(), cohort11.getId())
                .orElseGet(() -> {
                    CohortMember cm = CohortMember.builder()
                            .member(testMember)
                            .cohort(cohort11)
                            .part(Part.SERVER)
                            .build();
                    return cohortMemberRepository.save(cm);
                });

        testSession = Session.builder()
                .cohort(cohort11)
                .title("보증금 테스트 일정")
                .description("보증금 테스트 일정 설명")
                .startTime(LocalDateTime.of(2026, 3, 1, 14, 0))
                .location("강남")
                .status(SessionStatus.IN_PROGRESS)
                .build();
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("Admin API 19: 보증금 이력 조회")
    void testGetDepositHistory() throws Exception {
        // 출결 기록 생성 (패널티 발생)
        Attendance attendance = Attendance.builder()
                .member(testMember)
                .session(testSession)
                .status(AttendanceStatus.ABSENT)
                .penalty(10000)
                .reason("무단 결석")
                .build();
        attendance = attendanceRepository.save(attendance);

        // 패널티 차감
        depositService.deductPenalty(testMember.getId(), 10000, 
                "출결 등록 - ABSENT 패널티 10000원 (출결 ID: " + attendance.getId() + ")");

        mockMvc.perform(get("/api/v1/admin/cohort-members/{cohortMemberId}/deposits", cohortMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].cohortMemberId").value(cohortMember.getId()))
                .andExpect(jsonPath("$.data[0].type").exists())
                .andExpect(jsonPath("$.data[0].amount").exists())
                .andExpect(jsonPath("$.data[0].balanceAfter").exists())
                .andExpect(jsonPath("$.data[0].attendanceId").exists())
                .andExpect(jsonPath("$.data[0].description").exists())
                .andExpect(jsonPath("$.data[0].createdAt").exists());
    }
}
