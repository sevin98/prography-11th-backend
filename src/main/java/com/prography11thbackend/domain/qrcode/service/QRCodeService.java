package com.prography11thbackend.domain.qrcode.service;

import com.prography11thbackend.domain.qrcode.entity.QRCode;

public interface QRCodeService {

    QRCode createQRCode(Long sessionId);

    QRCode refreshQRCode(Long qrCodeId);
}
