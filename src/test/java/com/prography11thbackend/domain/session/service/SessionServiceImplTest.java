package com.prography11thbackend.domain.session.service;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.qrcode.entity.QRCode;
import com.prography11thbackend.domain.qrcode.repository.QRCodeRepository;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;
import com.prography11thbackend.domain.session.repository.SessionRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService 단위 테스트")
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private CohortRepository cohortRepository;

    @Mock
    private QRCodeRepository qrCodeRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private Cohort cohort;
    private Session session;

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
        cohort = Cohort.builder()
                .number(11)
                .build();
        setId(cohort, 1L);

        session = Session.builder()
                .title("테스트 세션")
                .description("")
                .startTime(LocalDateTime.now())
                .location("")
                .cohort(cohort)
                .status(SessionStatus.SCHEDULED)
                .build();
        setId(session, 1L);
    }

    @Test
    @DisplayName("일정 생성 성공")
    void createSession_Success() {
        // given
        String title = "새 세션";
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(14, 0);
        String location = "강의실 A";
        Long cohortId = 1L;

        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> {
            Session session = invocation.getArgument(0);
            Session saved = Session.builder()
                    .title(session.getTitle())
                    .description(session.getDescription())
                    .startTime(session.getStartTime())
                    .location(session.getLocation())
                    .cohort(session.getCohort())
                    .status(session.getStatus())
                    .build();
            setId(saved, 1L);
            return saved;
        });
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(invocation -> {
            QRCode qrCode = invocation.getArgument(0);
            QRCode saved = QRCode.builder()
                    .session(qrCode.getSession())
                    .build();
            setId(saved, 1L);
            try {
                Field hashValueField = QRCode.class.getDeclaredField("hashValue");
                hashValueField.setAccessible(true);
                hashValueField.set(saved, qrCode.getHashValue());
            } catch (Exception e) {
                throw new RuntimeException("Failed to set hashValue", e);
            }
            return saved;
        });

        // when
        Session result = sessionService.createSession(title, date, time, location, cohortId);

        // then
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getLocation()).isEqualTo(location);
        assertThat(result.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
        verify(sessionRepository, times(1)).save(any(Session.class));
        verify(qrCodeRepository, times(1)).save(any(QRCode.class));
    }

    @Test
    @DisplayName("일정 생성 실패 - 기수 없음")
    void createSession_Fail_CohortNotFound() {
        // given
        String title = "새 세션";
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(14, 0);
        String location = "강의실 A";
        Long cohortId = 999L;

        when(cohortRepository.findById(cohortId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sessionService.createSession(title, date, time, location, cohortId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COHORT_NOT_FOUND);
    }

    @Test
    @DisplayName("일정 수정 성공")
    void updateSession_Success() {
        // given
        Long sessionId = 1L;
        String newTitle = "수정된 세션";
        LocalDate newDate = LocalDate.now().plusDays(2);
        LocalTime newTime = LocalTime.of(15, 0);
        String newLocation = "강의실 B";
        SessionStatus newStatus = SessionStatus.IN_PROGRESS;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // when
        Session result = sessionService.updateSession(sessionId, newTitle, newDate, newTime, newLocation, newStatus);

        // then
        assertThat(result.getTitle()).isEqualTo(newTitle);
        assertThat(result.getLocation()).isEqualTo(newLocation);
        assertThat(result.getStatus()).isEqualTo(newStatus);
        verify(sessionRepository, times(1)).findById(sessionId);
    }

    @Test
    @DisplayName("일정 삭제 성공")
    void deleteSession_Success() {
        // given
        Long sessionId = 1L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // when
        Session result = sessionService.deleteSession(sessionId);

        // then
        assertThat(result.getStatus()).isEqualTo(SessionStatus.CANCELLED);
        verify(sessionRepository, times(1)).findById(sessionId);
    }

    @Test
    @DisplayName("일정 삭제 실패 - 이미 취소된 일정")
    void deleteSession_Fail_AlreadyCancelled() {
        // given
        Long sessionId = 1L;
        Session cancelledSession = Session.builder()
                .title("취소된 세션")
                .description("")
                .startTime(LocalDateTime.now())
                .location("")
                .cohort(cohort)
                .status(SessionStatus.CANCELLED)
                .build();
        setId(cancelledSession, 1L);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(cancelledSession));

        // when & then
        assertThatThrownBy(() -> sessionService.deleteSession(sessionId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_ALREADY_CANCELLED);
    }

    @Test
    @DisplayName("회원용 일정 목록 조회 성공 - CANCELLED 제외")
    void getSessionsForMember_Success() {
        // given
        Long cohortId = 1L;
        Session activeSession = Session.builder()
                .title("활성 세션")
                .description("")
                .startTime(LocalDateTime.now())
                .location("")
                .cohort(cohort)
                .status(SessionStatus.SCHEDULED)
                .build();
        setId(activeSession, 1L);
        Session cancelledSession = Session.builder()
                .title("취소된 세션")
                .description("")
                .startTime(LocalDateTime.now())
                .location("")
                .cohort(cohort)
                .status(SessionStatus.CANCELLED)
                .build();
        setId(cancelledSession, 2L);

        when(sessionRepository.findByCohortIdAndStatusNot(cohortId, SessionStatus.CANCELLED))
                .thenReturn(List.of(activeSession));

        // when
        List<Session> result = sessionService.getSessionsForMember(cohortId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isNotEqualTo(SessionStatus.CANCELLED);
        verify(sessionRepository, times(1)).findByCohortIdAndStatusNot(cohortId, SessionStatus.CANCELLED);
    }

    @Test
    @DisplayName("관리자용 일정 목록 조회 성공 - 전체 조회")
    void getSessionsForAdmin_Success() {
        // given
        Long cohortId = 1L;
        Session session1 = Session.builder()
                .title("세션 1")
                .description("")
                .startTime(LocalDateTime.now())
                .location("")
                .cohort(cohort)
                .status(SessionStatus.SCHEDULED)
                .build();
        setId(session1, 1L);
        Session session2 = Session.builder()
                .title("세션 2")
                .description("")
                .startTime(LocalDateTime.now())
                .location("")
                .cohort(cohort)
                .status(SessionStatus.CANCELLED)
                .build();
        setId(session2, 2L);

        when(sessionRepository.findByCohortId(cohortId))
                .thenReturn(List.of(session1, session2));

        // when
        List<Session> result = sessionService.getSessionsForAdmin(cohortId);

        // then
        assertThat(result).hasSize(2);
        verify(sessionRepository, times(1)).findByCohortId(cohortId);
    }
}
