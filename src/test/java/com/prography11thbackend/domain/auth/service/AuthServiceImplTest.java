package com.prography11thbackend.domain.auth.service;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberStatus;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private Member member;

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .loginId("testuser")
                .passwordHash("$2a$12$encoded")
                .name("테스트유저")
                .phone(null)
                .role(null)
                .status(MemberStatus.ACTIVE)
                .build();
        setId(member, 1L);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        String loginId = "testuser";
        String password = "password123";

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPasswordHash())).thenReturn(true);

        // when
        Member result = authService.login(loginId, password);

        // then
        assertThat(result).isEqualTo(member);
        verify(memberRepository, times(1)).findByLoginId(loginId);
        verify(passwordEncoder, times(1)).matches(password, member.getPasswordHash());
    }

    @Test
    @DisplayName("로그인 실패 - loginId 없음")
    void login_Fail_LoginIdNotFound() {
        // given
        String loginId = "nonexistent";
        String password = "password123";

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(loginId, password))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_PasswordMismatch() {
        // given
        String loginId = "testuser";
        String password = "wrongpassword";

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPasswordHash())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(loginId, password))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴한 회원")
    void login_Fail_WithdrawnMember() {
        // given
        String loginId = "testuser";
        String password = "password123";
        Member withdrawnMember = Member.builder()
                .loginId("testuser")
                .passwordHash("$2a$12$encoded")
                .name("테스트유저")
                .phone(null)
                .role(null)
                .status(MemberStatus.WITHDRAWN)
                .build();
        setId(withdrawnMember, 1L);

        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(withdrawnMember));

        // when & then
        assertThatThrownBy(() -> authService.login(loginId, password))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_WITHDRAWN);
    }
}
