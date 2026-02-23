package com.prography11thbackend.api.attendance;

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
import com.prography11thbackend.domain.qrcode.entity.QRCode;
import com.prography11thbackend.domain.qrcode.repository.QRCodeRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Attendance API 테스트")
class AttendanceControllerTest {

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
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private DepositService depositService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private Cohort cohort11;
    private Member testMember;
    private Session testSession;
    private QRCode testQRCode;
    private CohortMember cohortMember;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        testMember = memberRepository.findByLoginId("attendance_member_user")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .loginId("attendance_member_user")
                            .passwordHash("$2a$12$encoded")
                            .name("출석체크유저")
                            .phone("010-9999-0000")
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
                .title("출석체크 테스트 일정")
                .description("출석체크 테스트 일정 설명")
                .startTime(LocalDateTime.of(2026, 3, 1, 14, 0))
                .location("강남")
                .status(SessionStatus.IN_PROGRESS)
                .build();
        testSession = sessionRepository.save(testSession);

        testQRCode = QRCode.builder()
                .session(testSession)
                .build();
        testQRCode = qrCodeRepository.save(testQRCode);
    }

    @Test
    @DisplayName("Member API 04: QR 출석 체크")
    void testCheckAttendance() throws Exception {
        String requestBody = """
                {
                    "hashValue": "%s",
                    "memberId": %d
                }
                """.formatted(testQRCode.getHashValue(), testMember.getId());

        mockMvc.perform(post("/api/v1/attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.sessionId").value(testSession.getId()))
                .andExpect(jsonPath("$.data.memberId").value(testMember.getId()))
                .andExpect(jsonPath("$.data.status").value("PRESENT"))
                .andExpect(jsonPath("$.data.penaltyAmount").exists())
                .andExpect(jsonPath("$.data.checkedInAt").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("Member API 05: 내 출결 기록 조회")
    void testGetMyAttendances() throws Exception {
        // 출결 기록 생성
        Attendance attendance = Attendance.builder()
                .member(testMember)
                .session(testSession)
                .status(AttendanceStatus.PRESENT)
                .penalty(0)
                .checkedAt(LocalDateTime.now())
                .build();
        attendanceRepository.save(attendance);

        mockMvc.perform(get("/api/v1/attendances")
                        .param("memberId", String.valueOf(testMember.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].sessionId").exists())
                .andExpect(jsonPath("$.data[0].sessionTitle").exists())
                .andExpect(jsonPath("$.data[0].status").exists())
                .andExpect(jsonPath("$.data[0].memberId").doesNotExist())
                .andExpect(jsonPath("$.data[0].updatedAt").doesNotExist());
    }
}
