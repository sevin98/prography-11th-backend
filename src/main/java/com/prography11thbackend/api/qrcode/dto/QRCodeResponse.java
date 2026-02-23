package com.prography11thbackend.api.qrcode.dto;

import com.prography11thbackend.domain.qrcode.entity.QRCode;

import java.time.Instant;
import java.time.ZoneId;

public record QRCodeResponse(
        Long id,
        Long sessionId,
        String hashValue,
        Instant createdAt,
        Instant expiresAt
) {
    public static QRCodeResponse from(QRCode qrCode) {
        return new QRCodeResponse(
                qrCode.getId(),
                qrCode.getSession().getId(),
                qrCode.getHashValue(),
                qrCode.getCreatedAt() != null ? qrCode.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                qrCode.getExpiresAt() != null ? qrCode.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
