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
    public Session createSession(String title, java.time.LocalDate date, java.time.LocalTime time, String location, Long cohortId) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));

        java.time.LocalDateTime startTime = date != null && time != null 
                ? java.time.LocalDateTime.of(date, time) 
                : null;

        Session session = Session.builder()
                .title(title)
                .description("") // description은 명세서에 없으므로 빈 문자열
                .startTime(startTime)
                .location(location)
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
    public Session updateSession(Long id, String title, java.time.LocalDate date, java.time.LocalTime time, String location, com.prography11thbackend.domain.session.entity.SessionStatus status) {
        Session session = getSessionById(id);
        java.time.LocalDateTime startTime = date != null && time != null 
                ? java.time.LocalDateTime.of(date, time) 
                : null;
        session.update(title, null, startTime, location, status);
        return session;
    }

    @Override
    public Session deleteSession(Long id) {
        Session session = getSessionById(id);
        if (session.getStatus() == com.prography11thbackend.domain.session.entity.SessionStatus.CANCELLED) {
            throw new com.prography11thbackend.global.exception.BusinessException(com.prography11thbackend.global.exception.ErrorCode.SESSION_ALREADY_CANCELLED);
        }
        session.cancel();
        return session;
    }
}
