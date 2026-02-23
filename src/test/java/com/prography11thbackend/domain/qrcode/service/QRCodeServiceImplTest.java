package com.prography11thbackend.domain.qrcode.service;

import com.prography11thbackend.domain.cohort.entity.Cohort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QRCodeService 단위 테스트")
class QRCodeServiceImplTest {

    @Mock
    private QRCodeRepository qrCodeRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private QRCodeServiceImpl qrCodeService;

    private Session session;
    private QRCode existingQRCode;

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
        Cohort cohort = Cohort.builder()
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

        existingQRCode = QRCode.builder()
                .session(session)
                .build();
        setId(existingQRCode, 1L);
        try {
            Field hashValueField = QRCode.class.getDeclaredField("hashValue");
            hashValueField.setAccessible(true);
            hashValueField.set(existingQRCode, "existing-hash");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set hashValue", e);
        }
    }

    @Test
    @DisplayName("QR 코드 생성 성공 - 활성 QR 없음")
    void createQRCode_Success_NoActiveQR() {
        // given
        Long sessionId = 1L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(qrCodeRepository.findBySessionIdAndIsActiveTrue(sessionId)).thenReturn(Optional.empty());
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
        QRCode result = qrCodeService.createQRCode(sessionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSession()).isEqualTo(session);
        assertThat(result.getIsActive()).isTrue();
        verify(sessionRepository, times(1)).findById(sessionId);
        verify(qrCodeRepository, times(1)).findBySessionIdAndIsActiveTrue(sessionId);
        verify(qrCodeRepository, times(1)).save(any(QRCode.class));
    }

    @Test
    @DisplayName("QR 코드 생성 성공 - 기존 활성 QR 만료 처리")
    void createQRCode_Success_ExpireExistingQR() {
        // given
        Long sessionId = 1L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(qrCodeRepository.findBySessionIdAndIsActiveTrue(sessionId)).thenReturn(Optional.of(existingQRCode));
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(invocation -> {
            QRCode qrCode = invocation.getArgument(0);
            QRCode saved = QRCode.builder()
                    .session(qrCode.getSession())
                    .build();
            setId(saved, 2L);
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
        QRCode result = qrCodeService.createQRCode(sessionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSession()).isEqualTo(session);
        // existingQRCode는 실제 객체이므로 verify 대신 상태 확인
        assertThat(existingQRCode.getIsActive()).isFalse();
        verify(qrCodeRepository, times(1)).save(any(QRCode.class));
    }

    @Test
    @DisplayName("QR 코드 생성 실패 - 세션 없음")
    void createQRCode_Fail_SessionNotFound() {
        // given
        Long sessionId = 999L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> qrCodeService.createQRCode(sessionId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
    }

    @Test
    @DisplayName("QR 코드 갱신 성공")
    void refreshQRCode_Success() {
        // given
        Long qrCodeId = 1L;

        when(qrCodeRepository.findById(qrCodeId)).thenReturn(Optional.of(existingQRCode));
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(qrCodeRepository.findBySessionIdAndIsActiveTrue(session.getId())).thenReturn(Optional.empty());
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(invocation -> {
            QRCode qrCode = invocation.getArgument(0);
            QRCode saved = QRCode.builder()
                    .session(qrCode.getSession())
                    .build();
            setId(saved, 2L);
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
        QRCode result = qrCodeService.refreshQRCode(qrCodeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSession()).isEqualTo(session);
        // existingQRCode는 실제 객체이므로 verify 대신 상태 확인
        assertThat(existingQRCode.getIsActive()).isFalse();
        verify(qrCodeRepository, times(1)).save(any(QRCode.class));
    }

    @Test
    @DisplayName("QR 코드 갱신 실패 - QR 코드 없음")
    void refreshQRCode_Fail_QRNotFound() {
        // given
        Long qrCodeId = 999L;

        when(qrCodeRepository.findById(qrCodeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> qrCodeService.refreshQRCode(qrCodeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QR_NOT_FOUND);
    }
}
