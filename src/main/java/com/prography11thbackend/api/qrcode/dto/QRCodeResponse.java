package com.prography11thbackend.api.qrcode.dto;

import com.prography11thbackend.domain.qrcode.entity.QRCode;

import java.time.LocalDateTime;

public record QRCodeResponse(
        Long id,
        String hashValue,
        Long sessionId,
        LocalDateTime expiresAt,
        Boolean isActive
) {
    public static QRCodeResponse from(QRCode qrCode) {
        return new QRCodeResponse(
                qrCode.getId(),
                qrCode.getHashValue(),
                qrCode.getSession().getId(),
                qrCode.getExpiresAt(),
                qrCode.getIsActive()
        );
    }
}
