package com.prography11thbackend.api.member;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Member API 테스트")
class MemberControllerTest {

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
    private DepositService depositService;

    private Member testMember;
    private Cohort cohort11;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });
        
        testMember = memberRepository.findByLoginId("member_api_user")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .loginId("member_api_user")
                            .passwordHash("$2a$12$encoded")
                            .name("회원API유저")
                            .phone("010-7777-8888")
                            .status(MemberStatus.ACTIVE)
                            .role(MemberRole.MEMBER)
                            .build();
                    return memberRepository.save(member);
                });
        
        // CohortMember 생성 및 Deposit 초기화
        cohortMemberRepository.findByMemberIdAndCohortId(testMember.getId(), cohort11.getId())
                .orElseGet(() -> {
                    CohortMember cohortMember = CohortMember.builder()
                            .member(testMember)
                            .cohort(cohort11)
                            .part(Part.SERVER)
                            .build();
                    return cohortMemberRepository.save(cohortMember);
                });
        depositService.createInitialDeposit(testMember.getId());
    }

    @Test
    @DisplayName("Member API 02: 회원 조회")
    void testGetMember() throws Exception {
        mockMvc.perform(get("/api/v1/members/{id}", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testMember.getId()))
                .andExpect(jsonPath("$.data.loginId").value("member_api_user"))
                .andExpect(jsonPath("$.data.name").value("회원API유저"))
                .andExpect(jsonPath("$.data.phone").value("010-7777-8888"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.generation").doesNotExist())
                .andExpect(jsonPath("$.data.partName").doesNotExist())
                .andExpect(jsonPath("$.data.teamName").doesNotExist())
                .andExpect(jsonPath("$.data.deposit").doesNotExist())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("Member API 06: 출결 요약 조회")
    void testGetAttendanceSummary() throws Exception {
        mockMvc.perform(get("/api/v1/members/{id}/attendance-summary", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(testMember.getId()))
                .andExpect(jsonPath("$.data.present").exists())
                .andExpect(jsonPath("$.data.absent").exists())
                .andExpect(jsonPath("$.data.late").exists())
                .andExpect(jsonPath("$.data.excused").exists())
                .andExpect(jsonPath("$.data.totalPenalty").exists())
                .andExpect(jsonPath("$.data.deposit").exists());
    }
}
