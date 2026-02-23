package com.prography11thbackend.api.qrcode;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
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
@DisplayName("Admin QRCode API 테스트")
class AdminQRCodeControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    private Cohort cohort11;
    private Session testSession;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        testSession = Session.builder()
                .cohort(cohort11)
                .title("QR 테스트 일정")
                .description("QR 테스트 일정 설명")
                .startTime(LocalDateTime.of(2026, 3, 1, 14, 0))
                .location("강남")
                .status(SessionStatus.SCHEDULED)
                .build();
        testSession = sessionRepository.save(testSession);
    }

    @Test
    @DisplayName("Admin API 12: QR 코드 생성")
    void testCreateQRCode() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sessions/{sessionId}/qrcodes", testSession.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.sessionId").value(testSession.getId()))
                .andExpect(jsonPath("$.data.hashValue").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andExpect(jsonPath("$.data.isActive").doesNotExist());
    }

    @Test
    @DisplayName("Admin API 13: QR 코드 갱신")
    void testRenewQRCode() throws Exception {
        // 기존 QR 코드 생성
        QRCode existingQR = QRCode.builder()
                .session(testSession)
                .build();
        existingQR = qrCodeRepository.save(existingQR);

        mockMvc.perform(put("/api/v1/admin/qrcodes/{id}", existingQR.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.sessionId").value(testSession.getId()))
                .andExpect(jsonPath("$.data.hashValue").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.expiresAt").exists());
    }
}
