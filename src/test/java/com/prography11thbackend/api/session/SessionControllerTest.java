package com.prography11thbackend.api.session;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
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
@DisplayName("Session API 테스트")
class SessionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CohortRepository cohortRepository;

    private Cohort cohort11;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        // SCHEDULED 일정 생성
        Session scheduledSession = Session.builder()
                .cohort(cohort11)
                .title("예정된 일정")
                .description("예정된 일정 설명")
                .startTime(LocalDateTime.of(2026, 3, 15, 14, 0))
                .location("강남")
                .status(SessionStatus.SCHEDULED)
                .build();
        sessionRepository.save(scheduledSession);

        // CANCELLED 일정 생성 (제외되어야 함)
        Session cancelledSession = Session.builder()
                .cohort(cohort11)
                .title("취소된 일정")
                .description("취소된 일정 설명")
                .startTime(LocalDateTime.of(2026, 3, 20, 14, 0))
                .location("서초")
                .status(SessionStatus.CANCELLED)
                .build();
        sessionRepository.save(cancelledSession);
    }

    @Test
    @DisplayName("Member API 03: 일정 목록 조회 (CANCELLED 제외)")
    void testGetSessions() throws Exception {
        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].title").exists())
                .andExpect(jsonPath("$.data[0].date").exists())
                .andExpect(jsonPath("$.data[0].time").exists())
                .andExpect(jsonPath("$.data[0].location").exists())
                .andExpect(jsonPath("$.data[0].status").exists())
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].updatedAt").exists())
                .andExpect(jsonPath("$.data[?(@.status == 'CANCELLED')]").doesNotExist());
    }
}
