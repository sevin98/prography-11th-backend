package com.prography11thbackend.api.auth;

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
import org.springframework.security.crypto.password.PasswordEncoder;
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
@DisplayName("Auth API 테스트")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member testMember;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String encodedPassword = passwordEncoder.encode("password123");
        testMember = memberRepository.findByLoginId("login_test_user")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .loginId("login_test_user")
                            .passwordHash(encodedPassword)
                            .name("로그인테스트유저")
                            .phone("010-5555-6666")
                            .status(MemberStatus.ACTIVE)
                            .role(MemberRole.MEMBER)
                            .build();
                    return memberRepository.save(member);
                });
    }

    @Test
    @DisplayName("Member API 01: 로그인 성공")
    void testLoginSuccess() throws Exception {
        String requestBody = """
                {
                    "loginId": "login_test_user",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testMember.getId()))
                .andExpect(jsonPath("$.data.loginId").value("login_test_user"))
                .andExpect(jsonPath("$.data.name").value("로그인테스트유저"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("Member API 01: 로그인 실패 - 잘못된 비밀번호")
    void testLoginFailureWrongPassword() throws Exception {
        String requestBody = """
                {
                    "loginId": "login_test_user",
                    "password": "wrong_password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").exists());
    }
}
