package com.prography11thbackend.api.cohort;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.Team;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
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
@DisplayName("Admin Cohort API 테스트")
class AdminCohortControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private TeamRepository teamRepository;

    private Cohort cohort11;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        cohort11 = cohortRepository.findByNumber(11)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder().number(11).build();
                    return cohortRepository.save(cohort);
                });

        // 팀 생성
        Team teamA = Team.builder()
                .cohort(cohort11)
                .name("Team A")
                .build();
        teamRepository.save(teamA);
    }

    @Test
    @DisplayName("Admin API 06: 기수 목록 조회")
    void testGetAllCohorts() throws Exception {
        mockMvc.perform(get("/api/v1/admin/cohorts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].generation").exists())
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].createdAt").exists());
    }

    @Test
    @DisplayName("Admin API 07: 기수 상세 조회")
    void testGetCohortDetail() throws Exception {
        mockMvc.perform(get("/api/v1/admin/cohorts/{id}", cohort11.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(cohort11.getId()))
                .andExpect(jsonPath("$.data.generation").value(11))
                .andExpect(jsonPath("$.data.name").value("11기"))
                .andExpect(jsonPath("$.data.parts").isArray())
                .andExpect(jsonPath("$.data.parts[0].id").exists())
                .andExpect(jsonPath("$.data.parts[0].name").exists())
                .andExpect(jsonPath("$.data.teams").isArray())
                .andExpect(jsonPath("$.data.createdAt").exists());
    }
}
