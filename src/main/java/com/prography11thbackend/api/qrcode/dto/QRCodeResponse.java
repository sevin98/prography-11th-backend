package com.prography11thbackend.api.qrcode.dto;

import com.prography11thbackend.domain.qrcode.entity.QRCode;

import java.time.Instant;
import java.time.ZoneOffset;

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
                qrCode.getCreatedAt() != null ? qrCode.getCreatedAt().toInstant(ZoneOffset.UTC) : null,
                qrCode.getExpiresAt() != null ? qrCode.getExpiresAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
