package com.prography11thbackend.api.attendance;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;
import com.prography11thbackend.domain.attendance.repository.AttendanceRepository;
import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.entity.Part;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
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
import org.springframework.http.MediaType;
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
@DisplayName("Admin Attendance API 테스트")
class AdminAttendanceControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private TeamRepository teamRepository;

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
    private Session testSession;
    private CohortMember cohortMember;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        testMember = memberRepository.findByLoginId("attendance_test_user")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .loginId("attendance_test_user")
                            .passwordHash("$2a$12$encoded")
                            .name("출결테스트유저")
                            .phone("010-1111-2222")
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
                .title("출결 테스트 일정")
                .description("출결 테스트 일정 설명")
                .startTime(LocalDateTime.of(2026, 3, 1, 14, 0))
                .location("강남")
                .status(SessionStatus.IN_PROGRESS)
                .build();
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("Admin API 14: 출결 등록")
    void testCreateAttendance() throws Exception {
        String requestBody = """
                {
                    "sessionId": %d,
                    "memberId": %d,
                    "status": "ABSENT",
                    "lateMinutes": null,
                    "reason": "무단 결석"
                }
                """.formatted(testSession.getId(), testMember.getId());

        mockMvc.perform(post("/api/v1/admin/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.sessionId").value(testSession.getId()))
                .andExpect(jsonPath("$.data.memberId").value(testMember.getId()))
                .andExpect(jsonPath("$.data.status").value("ABSENT"))
                .andExpect(jsonPath("$.data.penaltyAmount").value(10000))
                .andExpect(jsonPath("$.data.reason").value("무단 결석"))
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("Admin API 15: 출결 수정")
    void testUpdateAttendance() throws Exception {
        Attendance attendance = Attendance.builder()
                .member(testMember)
                .session(testSession)
                .status(AttendanceStatus.ABSENT)
                .penalty(10000)
                .reason("무단 결석")
                .build();
        attendance = attendanceRepository.save(attendance);

        String requestBody = """
                {
                    "status": "EXCUSED",
                    "lateMinutes": null,
                    "reason": "병가"
                }
                """;

        mockMvc.perform(put("/api/v1/admin/attendances/{id}", attendance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("EXCUSED"))
                .andExpect(jsonPath("$.data.penaltyAmount").value(0))
                .andExpect(jsonPath("$.data.reason").value("병가"));
    }

    @Test
    @DisplayName("Admin API 16: 일정별 출결 요약")
    void testGetAttendanceSummaryBySession() throws Exception {
        // 출결 기록 추가
        Attendance attendance1 = Attendance.builder()
                .member(testMember)
                .session(testSession)
                .status(AttendanceStatus.PRESENT)
                .penalty(0)
                .build();
        attendanceRepository.save(attendance1);

        mockMvc.perform(get("/api/v1/admin/attendances/sessions/{sessionId}/summary", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].memberId").exists())
                .andExpect(jsonPath("$.data[0].memberName").exists())
                .andExpect(jsonPath("$.data[0].present").exists())
                .andExpect(jsonPath("$.data[0].deposit").exists());
    }

    @Test
    @DisplayName("Admin API 17: 회원 출결 상세")
    void testGetMemberAttendanceDetail() throws Exception {
        Attendance attendance = Attendance.builder()
                .member(testMember)
                .session(testSession)
                .status(AttendanceStatus.PRESENT)
                .penalty(0)
                .build();
        attendanceRepository.save(attendance);

        mockMvc.perform(get("/api/v1/admin/attendances/members/{memberId}", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(testMember.getId()))
                .andExpect(jsonPath("$.data.memberName").value("출결테스트유저"))
                .andExpect(jsonPath("$.data.generation").value(11))
                .andExpect(jsonPath("$.data.deposit").exists())
                .andExpect(jsonPath("$.data.excuseCount").exists())
                .andExpect(jsonPath("$.data.attendances").isArray())
                .andExpect(jsonPath("$.data.attendances[0].memberId").exists())
                .andExpect(jsonPath("$.data.attendances[0].updatedAt").exists());
    }

    @Test
    @DisplayName("Admin API 18: 일정별 출결 목록")
    void testGetSessionAttendances() throws Exception {
        Attendance attendance = Attendance.builder()
                .member(testMember)
                .session(testSession)
                .status(AttendanceStatus.LATE)
                .penalty(5000)
                .checkedAt(LocalDateTime.now())
                .build();
        attendanceRepository.save(attendance);

        mockMvc.perform(get("/api/v1/admin/attendances/sessions/{sessionId}", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(testSession.getId()))
                .andExpect(jsonPath("$.data.sessionTitle").value("출결 테스트 일정"))
                .andExpect(jsonPath("$.data.attendances").isArray())
                .andExpect(jsonPath("$.data.attendances[0].id").exists())
                .andExpect(jsonPath("$.data.attendances[0].memberId").exists())
                .andExpect(jsonPath("$.data.attendances[0].updatedAt").exists());
    }
}
