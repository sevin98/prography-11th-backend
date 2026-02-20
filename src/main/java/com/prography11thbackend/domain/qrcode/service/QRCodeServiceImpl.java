package com.prography11thbackend.domain.qrcode.service;

import com.prography11thbackend.domain.qrcode.entity.QRCode;
import com.prography11thbackend.domain.qrcode.repository.QRCodeRepository;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.repository.SessionRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QRCodeServiceImpl implements QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final SessionRepository sessionRepository;

    @Override
    public QRCode createQRCode(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        // 활성 QR 코드가 있으면 만료 처리
        qrCodeRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .ifPresent(QRCode::expire);

        QRCode qrCode = QRCode.builder()
                .session(session)
                .build();

        return qrCodeRepository.save(qrCode);
    }

    @Override
    public QRCode refreshQRCode(Long qrCodeId) {
        QRCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_NOT_FOUND));

        // 기존 QR 만료
        qrCode.expire();

        // 새 QR 생성
        return createQRCode(qrCode.getSession().getId());
    }
}
