package com.prography11thbackend.api.qrcode.controller;

import com.prography11thbackend.api.qrcode.dto.QRCodeResponse;
import com.prography11thbackend.domain.qrcode.entity.QRCode;
import com.prography11thbackend.domain.qrcode.service.QRCodeService;
import com.prography11thbackend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminQRCodeController {

    private final QRCodeService qrCodeService;

    @PostMapping("/sessions/{sessionId}/qrcodes")
    public ResponseEntity<ApiResponse<QRCodeResponse>> createQRCode(@PathVariable Long sessionId) {
        QRCode qrCode = qrCodeService.createQRCode(sessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(QRCodeResponse.from(qrCode)));
    }

    @PutMapping("/qrcodes/{id}")
    public ResponseEntity<ApiResponse<QRCodeResponse>> refreshQRCode(@PathVariable Long id) {
        QRCode qrCode = qrCodeService.refreshQRCode(id);
        return ResponseEntity.ok(ApiResponse.success(QRCodeResponse.from(qrCode)));
    }
}
