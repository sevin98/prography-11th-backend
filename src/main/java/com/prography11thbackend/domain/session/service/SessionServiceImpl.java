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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final CohortRepository cohortRepository;
    private final QRCodeRepository qrCodeRepository;

    @Override
    public Session createSession(String title, String description, java.time.LocalDateTime startTime, Long cohortId) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));

        Session session = Session.builder()
                .title(title)
                .description(description)
                .startTime(startTime)
                .cohort(cohort)
                .status(SessionStatus.SCHEDULED)
                .build();

        Session savedSession = sessionRepository.save(session);

        // 일정 생성 시 QR 코드 자동 생성
        QRCode qrCode = QRCode.builder()
                .session(savedSession)
                .build();
        qrCodeRepository.save(qrCode);

        return savedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getSessionsForMember(Long cohortId) {
        // 회원용: CANCELLED 제외
        return sessionRepository.findByCohortIdAndStatusNot(cohortId, SessionStatus.CANCELLED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getSessionsForAdmin(Long cohortId) {
        // 관리자용: 전체 조회
        return sessionRepository.findByCohortId(cohortId);
    }

    @Override
    @Transactional(readOnly = true)
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }

    @Override
    public Session updateSession(Long id, String title, String description, java.time.LocalDateTime startTime) {
        Session session = getSessionById(id);
        session.update(title, description, startTime);
        return session;
    }

    @Override
    public void deleteSession(Long id) {
        Session session = getSessionById(id);
        session.cancel();
    }
}
