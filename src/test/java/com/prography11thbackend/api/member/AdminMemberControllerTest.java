package com.prography11thbackend.api.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.entity.Part;
import com.prography11thbackend.domain.cohort.entity.Team;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
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
import org.springframework.http.MediaType;
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
@DisplayName("Admin Member API 테스트")
class AdminMemberControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CohortMemberRepository cohortMemberRepository;

    @Autowired
    private DepositService depositService;

    private Cohort cohort11;
    private Team teamA;
    private Member adminMember;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = new ObjectMapper();
        // 11기 생성
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        // 팀 생성
        teamA = teamRepository.findByCohortIdAndName(cohort11.getId(), "Team A")
                .orElseGet(() -> {
                    Team team = Team.builder()
                            .cohort(cohort11)
                            .name("Team A")
                            .build();
                    return teamRepository.save(team);
                });

        // Admin 회원 생성
        adminMember = memberRepository.findByLoginId("admin")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .loginId("admin")
                            .passwordHash("$2a$12$encodedPassword")
                            .name("관리자")
                            .phone("010-0000-0000")
                            .status(MemberStatus.ACTIVE)
                            .role(MemberRole.ADMIN)
                            .build();
                    return memberRepository.save(member);
                });
    }

    @Test
    @DisplayName("Admin API 01: 회원 등록")
    void testCreateMember() throws Exception {
        String requestBody = """
                {
                    "loginId": "testuser1",
                    "password": "password123",
                    "name": "테스트유저1",
                    "phone": "010-1111-2222",
                    "cohortId": %d,
                    "partId": 6,
                    "teamId": %d
                }
                """.formatted(cohort11.getId(), teamA.getId());

        mockMvc.perform(post("/api/v1/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.loginId").value("testuser1"))
                .andExpect(jsonPath("$.data.name").value("테스트유저1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.generation").value(11))
                .andExpect(jsonPath("$.data.partName").value("SERVER"))
                .andExpect(jsonPath("$.data.deposit").doesNotExist());
    }

    @Test
    @DisplayName("Admin API 02: 회원 대시보드 조회")
    void testGetMembersDashboard() throws Exception {
        // 테스트 회원 생성
        Member member = Member.builder()
                .loginId("dashboard_user")
                            .passwordHash("$2a$12$encoded")
                .name("대시보드유저")
                .phone("010-9999-8888")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.MEMBER)
                .build();
        member = memberRepository.save(member);

        CohortMember cohortMember = CohortMember.builder()
                .member(member)
                .cohort(cohort11)
                .part(Part.SERVER)
                .team(teamA)
                .build();
        cohortMemberRepository.save(cohortMember);
        depositService.createInitialDeposit(member.getId());

        mockMvc.perform(get("/api/v1/admin/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("generation", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.content[0].deposit").exists());
    }

    @Test
    @DisplayName("Admin API 03: 회원 상세 조회")
    void testGetMemberDetail() throws Exception {
        Member member = Member.builder()
                .loginId("detail_user")
                            .passwordHash("$2a$12$encoded")
                .name("상세유저")
                .phone("010-7777-6666")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.MEMBER)
                .build();
        member = memberRepository.save(member);

        CohortMember cohortMember = CohortMember.builder()
                .member(member)
                .cohort(cohort11)
                .part(Part.WEB)
                .team(teamA)
                .build();
        cohortMemberRepository.save(cohortMember);

        mockMvc.perform(get("/api/v1/admin/members/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.loginId").value("detail_user"))
                .andExpect(jsonPath("$.data.generation").value(11))
                .andExpect(jsonPath("$.data.partName").value("WEB"))
                .andExpect(jsonPath("$.data.deposit").doesNotExist());
    }

    @Test
    @DisplayName("Admin API 04: 회원 수정")
    void testUpdateMember() throws Exception {
        Member member = Member.builder()
                .loginId("update_user")
                            .passwordHash("$2a$12$encoded")
                .name("수정전")
                .phone("010-1111-1111")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.MEMBER)
                .build();
        member = memberRepository.save(member);

        CohortMember cohortMember = CohortMember.builder()
                .member(member)
                .cohort(cohort11)
                .part(Part.SERVER)
                .team(teamA)
                .build();
        cohortMemberRepository.save(cohortMember);

        String requestBody = """
                {
                    "name": "수정후",
                    "phone": "010-2222-2222",
                    "cohortId": %d,
                    "partId": 7,
                    "teamId": %d
                }
                """.formatted(cohort11.getId(), teamA.getId());

        mockMvc.perform(put("/api/v1/admin/members/{id}", member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수정후"))
                .andExpect(jsonPath("$.data.phone").value("010-2222-2222"))
                .andExpect(jsonPath("$.data.partName").value("WEB"));
    }

    @Test
    @DisplayName("Admin API 05: 회원 탈퇴")
    void testDeleteMember() throws Exception {
        Member member = Member.builder()
                .loginId("withdraw_user")
                            .passwordHash("$2a$12$encoded")
                .name("탈퇴유저")
                .phone("010-3333-3333")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.MEMBER)
                .build();
        member = memberRepository.save(member);

        mockMvc.perform(delete("/api/v1/admin/members/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"))
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }
}
