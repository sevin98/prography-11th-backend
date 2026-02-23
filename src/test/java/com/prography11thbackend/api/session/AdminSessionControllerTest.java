package com.prography11thbackend.api.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;
import com.prography11thbackend.domain.attendance.repository.AttendanceRepository;
import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;
import com.prography11thbackend.domain.member.repository.MemberRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin Session API 테스트")
class AdminSessionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    private Cohort cohort11;
    private Member testMember;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = new ObjectMapper();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        testMember = memberRepository.findByLoginId("test_member")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .loginId("test_member")
                            .passwordHash("$2a$12$encoded")
                            .name("테스트회원")
                            .phone("010-1234-5678")
                            .status(MemberStatus.ACTIVE)
                            .role(MemberRole.MEMBER)
                            .build();
                    return memberRepository.save(member);
                });
    }

    @Test
    @DisplayName("Admin API 08: 일정 목록 조회")
    void testGetSessions() throws Exception {
        Session session = Session.builder()
                .cohort(cohort11)
                .title("테스트 일정")
                .description("테스트 일정 설명")
                .startTime(java.time.LocalDateTime.of(2026, 3, 1, 14, 0))
                .location("강남")
                .status(SessionStatus.SCHEDULED)
                .build();
        sessionRepository.save(session);

        mockMvc.perform(get("/api/v1/admin/sessions")
                        .param("dateFrom", "2026-03-01")
                        .param("dateTo", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].date").exists())
                .andExpect(jsonPath("$.data[0].time").exists())
                .andExpect(jsonPath("$.data[0].location").exists())
                .andExpect(jsonPath("$.data[0].attendanceSummary").exists())
                .andExpect(jsonPath("$.data[0].qrActive").exists());
    }

    @Test
    @DisplayName("Admin API 09: 일정 생성")
    void testCreateSession() throws Exception {
        String requestBody = """
                {
                    "title": "새 일정",
                    "date": "2026-03-15",
                    "time": "14:00",
                    "location": "서초"
                }
                """;

        mockMvc.perform(post("/api/v1/admin/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("새 일정"))
                .andExpect(jsonPath("$.data.date").value("2026-03-15"))
                .andExpect(jsonPath("$.data.time").value("14:00:00"))
                .andExpect(jsonPath("$.data.location").value("서초"))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.attendanceSummary").exists())
                .andExpect(jsonPath("$.data.qrActive").value(true));
    }

    @Test
    @DisplayName("Admin API 10: 일정 수정")
    void testUpdateSession() throws Exception {
        Session session = Session.builder()
                .cohort(cohort11)
                .title("수정전 일정")
                .description("수정전 일정 설명")
                .startTime(java.time.LocalDateTime.of(2026, 3, 1, 14, 0))
                .location("강남")
                .status(SessionStatus.SCHEDULED)
                .build();
        session = sessionRepository.save(session);

        String requestBody = """
                {
                    "title": "수정후 일정",
                    "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(put("/api/v1/admin/sessions/{id}", session.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("수정후 일정"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Admin API 11: 일정 삭제(취소)")
    void testDeleteSession() throws Exception {
        Session session = Session.builder()
                .cohort(cohort11)
                .title("취소할 일정")
                .description("취소할 일정 설명")
                .startTime(java.time.LocalDateTime.of(2026, 3, 1, 14, 0))
                .location("강남")
                .status(SessionStatus.SCHEDULED)
                .build();
        session = sessionRepository.save(session);

        // 출결 기록 추가
        Attendance attendance = Attendance.builder()
                .member(testMember)
                .session(session)
                .status(AttendanceStatus.PRESENT)
                .penalty(0)
                .build();
        attendanceRepository.save(attendance);

        mockMvc.perform(delete("/api/v1/admin/sessions/{id}", session.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.attendanceSummary").exists());
    }
}
