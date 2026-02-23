package com.prography11thbackend.domain.attendance.service;

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
import com.prography11thbackend.domain.member.entity.MemberStatus;
import com.prography11thbackend.domain.member.repository.MemberRepository;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService 단위 테스트")
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private QRCodeRepository qrCodeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CohortMemberRepository cohortMemberRepository;

    @Mock
    private CohortRepository cohortRepository;

    @Mock
    private DepositService depositService;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Member member;
    private Session session;
    private QRCode qrCode;
    private Cohort cohort;
    private CohortMember cohortMember;

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

        member = Member.builder()
                .loginId("testuser")
                .passwordHash("encoded")
                .name("테스트유저")
                .phone("010-1234-5678")
                .status(MemberStatus.ACTIVE)
                .build();
        setId(member, 1L);

        session = Session.builder()
                .title("테스트 세션")
                .description("")
                .startTime(LocalDateTime.now().minusHours(1))
                .location("")
                .cohort(cohort)
                .status(SessionStatus.IN_PROGRESS)
                .build();
        setId(session, 1L);

        qrCode = QRCode.builder()
                .session(session)
                .build();
        setId(qrCode, 1L);
        // hashValue를 Reflection으로 설정
        try {
            Field hashValueField = QRCode.class.getDeclaredField("hashValue");
            hashValueField.setAccessible(true);
            hashValueField.set(qrCode, "test-hash");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set hashValue", e);
        }

        cohortMember = CohortMember.builder()
                .member(member)
                .cohort(cohort)
                .part(Part.SERVER)
                .build();
        setId(cohortMember, 1L);
    }

    @Test
    @DisplayName("QR 코드로 출석 체크 성공 - PRESENT")
    void checkAttendanceByQR_Success_Present() {
        // given
        String qrHashValue = "test-hash";
        Long memberId = 1L;
        LocalDateTime sessionStartTime = LocalDateTime.now().plusHours(1); // 미래 시간
        session = Session.builder()
                .title("테스트 세션")
                .description("")
                .startTime(sessionStartTime)
                .location("")
                .cohort(cohort)
                .status(SessionStatus.IN_PROGRESS)
                .build();
        setId(session, 1L);
        qrCode = QRCode.builder()
                .session(session)
                .build();
        setId(qrCode, 1L);
        try {
            Field hashValueField = QRCode.class.getDeclaredField("hashValue");
            hashValueField.setAccessible(true);
            hashValueField.set(qrCode, qrHashValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set hashValue", e);
        }

        when(qrCodeRepository.findByHashValue(qrHashValue)).thenReturn(Optional.of(qrCode));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMemberIdAndSessionId(memberId, session.getId())).thenReturn(Optional.empty());
        when(cohortMemberRepository.findByMemberIdAndCohortId(memberId, cohort.getId())).thenReturn(Optional.of(cohortMember));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Attendance result = attendanceService.checkAttendanceByQR(qrHashValue, memberId);

        // then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.getPenalty()).isEqualTo(0);
        verify(depositService, never()).deductPenalty(anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("QR 코드로 출석 체크 성공 - LATE")
    void checkAttendanceByQR_Success_Late() {
        // given
        String qrHashValue = "test-hash";
        Long memberId = 1L;
        LocalDateTime sessionStartTime = LocalDateTime.now().minusMinutes(10); // 10분 전
        session = Session.builder()
                .title("테스트 세션")
                .description("")
                .startTime(sessionStartTime)
                .location("")
                .cohort(cohort)
                .status(SessionStatus.IN_PROGRESS)
                .build();
        setId(session, 1L);
        qrCode = QRCode.builder()
                .session(session)
                .build();
        setId(qrCode, 1L);
        try {
            Field hashValueField = QRCode.class.getDeclaredField("hashValue");
            hashValueField.setAccessible(true);
            hashValueField.set(qrCode, qrHashValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set hashValue", e);
        }

        when(qrCodeRepository.findByHashValue(qrHashValue)).thenReturn(Optional.of(qrCode));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMemberIdAndSessionId(memberId, session.getId())).thenReturn(Optional.empty());
        when(cohortMemberRepository.findByMemberIdAndCohortId(memberId, cohort.getId())).thenReturn(Optional.of(cohortMember));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance attendance = invocation.getArgument(0);
            Attendance saved = Attendance.builder()
                    .member(attendance.getMember())
                    .session(attendance.getSession())
                    .status(attendance.getStatus())
                    .penalty(attendance.getPenalty())
                    .checkedAt(attendance.getCheckedAt())
                    .reason(attendance.getReason())
                    .build();
            setId(saved, 1L);
            return saved;
        });

        // when
        Attendance result = attendanceService.checkAttendanceByQR(qrHashValue, memberId);

        // then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.LATE);
        assertThat(result.getPenalty()).isGreaterThan(0);
        verify(depositService, times(1)).deductPenalty(eq(memberId), anyInt(), anyString());
    }

    @Test
    @DisplayName("QR 코드로 출석 체크 실패 - QR 코드 없음")
    void checkAttendanceByQR_Fail_QRNotFound() {
        // given
        String qrHashValue = "invalid-hash";
        Long memberId = 1L;

        when(qrCodeRepository.findByHashValue(qrHashValue)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.checkAttendanceByQR(qrHashValue, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QR_INVALID);
    }

    @Test
    @DisplayName("QR 코드로 출석 체크 실패 - QR 만료")
    void checkAttendanceByQR_Fail_QRExpired() {
        // given
        String qrHashValue = "test-hash";
        Long memberId = 1L;
        qrCode.expire();

        when(qrCodeRepository.findByHashValue(qrHashValue)).thenReturn(Optional.of(qrCode));

        // when & then
        assertThatThrownBy(() -> attendanceService.checkAttendanceByQR(qrHashValue, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QR_EXPIRED);
    }

    @Test
    @DisplayName("QR 코드로 출석 체크 실패 - 중복 출결")
    void checkAttendanceByQR_Fail_DuplicateAttendance() {
        // given
        String qrHashValue = "test-hash";
        Long memberId = 1L;
        Attendance existingAttendance = Attendance.builder()
                .member(member)
                .session(session)
                .status(AttendanceStatus.PRESENT)
                .penalty(0)
                .checkedAt(null)
                .reason(null)
                .build();
        setId(existingAttendance, 1L);

        when(qrCodeRepository.findByHashValue(qrHashValue)).thenReturn(Optional.of(qrCode));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMemberIdAndSessionId(memberId, session.getId()))
                .thenReturn(Optional.of(existingAttendance));

        // when & then
        assertThatThrownBy(() -> attendanceService.checkAttendanceByQR(qrHashValue, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ATTENDANCE_ALREADY_CHECKED);
    }

    @Test
    @DisplayName("출결 수동 등록 성공 - ABSENT")
    void createAttendance_Success_Absent() {
        // given
        Long sessionId = 1L;
        Long memberId = 1L;
        String status = "ABSENT";
        Integer lateMinutes = null;
        String reason = "개인 사정";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceRepository.findByMemberIdAndSessionId(memberId, sessionId)).thenReturn(Optional.empty());
        when(cohortMemberRepository.findByMemberIdAndCohortId(memberId, cohort.getId())).thenReturn(Optional.of(cohortMember));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance attendance = invocation.getArgument(0);
            return Attendance.builder()
                    .member(attendance.getMember())
                    .session(attendance.getSession())
                    .status(attendance.getStatus())
                    .penalty(attendance.getPenalty())
                    .reason(attendance.getReason())
                    .build();
        });

        // when
        Attendance result = attendanceService.createAttendance(sessionId, memberId, status, lateMinutes, reason);

        // then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(result.getPenalty()).isEqualTo(10000);
        assertThat(result.getReason()).isEqualTo(reason);
        verify(depositService, times(1)).deductPenalty(eq(memberId), eq(10000), anyString());
    }

    @Test
    @DisplayName("출결 수동 등록 성공 - EXCUSED")
    void createAttendance_Success_Excused() {
        // given
        Long sessionId = 1L;
        Long memberId = 1L;
        String status = "EXCUSED";
        Integer lateMinutes = null;
        String reason = "공결 사유";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceRepository.findByMemberIdAndSessionId(memberId, sessionId)).thenReturn(Optional.empty());
        when(cohortMemberRepository.findByMemberIdAndCohortId(memberId, cohort.getId())).thenReturn(Optional.of(cohortMember));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance attendance = invocation.getArgument(0);
            return Attendance.builder()
                    .member(attendance.getMember())
                    .session(attendance.getSession())
                    .status(attendance.getStatus())
                    .penalty(attendance.getPenalty())
                    .reason(attendance.getReason())
                    .build();
        });

        // when
        Attendance result = attendanceService.createAttendance(sessionId, memberId, status, lateMinutes, reason);

        // then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.EXCUSED);
        assertThat(result.getPenalty()).isEqualTo(0);
        assertThat(result.getReason()).isEqualTo(reason);
        verify(depositService, never()).deductPenalty(anyLong(), anyInt(), anyString());
        // EXCUSED일 때는 createAttendance와 checkExcuseLimit에서 각각 호출되므로 2번
        verify(cohortMemberRepository, times(2)).findByMemberIdAndCohortId(memberId, cohort.getId());
    }

    @Test
    @DisplayName("출결 수정 성공 - 패널티 증가")
    void updateAttendance_Success_PenaltyIncrease() {
        // given
        Long attendanceId = 1L;
        String newStatus = "ABSENT";
        Integer lateMinutes = null;
        String reason = "수정 사유";

        Attendance existingAttendance = Attendance.builder()
                .member(member)
                .session(session)
                .status(AttendanceStatus.PRESENT)
                .penalty(0)
                .checkedAt(null)
                .reason(null)
                .build();
        setId(existingAttendance, attendanceId);

        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(existingAttendance));
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(cohortMemberRepository.findByMemberIdAndCohortId(member.getId(), cohort.getId()))
                .thenReturn(Optional.of(cohortMember));

        // when
        Attendance result = attendanceService.updateAttendance(attendanceId, newStatus, lateMinutes, reason);

        // then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(result.getPenalty()).isEqualTo(10000);
        verify(depositService, times(1)).deductPenalty(eq(member.getId()), eq(10000), anyString());
    }

    @Test
    @DisplayName("출결 수정 성공 - 패널티 감소 (환급)")
    void updateAttendance_Success_PenaltyDecrease() {
        // given
        Long attendanceId = 1L;
        String newStatus = "PRESENT";
        Integer lateMinutes = null;
        String reason = "수정 사유";

        Attendance existingAttendance = Attendance.builder()
                .member(member)
                .session(session)
                .status(AttendanceStatus.ABSENT)
                .penalty(10000)
                .checkedAt(null)
                .reason(null)
                .build();
        setId(existingAttendance, attendanceId);

        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(existingAttendance));
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(cohortMemberRepository.findByMemberIdAndCohortId(member.getId(), cohort.getId()))
                .thenReturn(Optional.of(cohortMember));

        // when
        Attendance result = attendanceService.updateAttendance(attendanceId, newStatus, lateMinutes, reason);

        // then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.getPenalty()).isEqualTo(0);
        verify(depositService, times(1)).refundPenalty(eq(member.getId()), eq(10000), anyString());
    }
}
