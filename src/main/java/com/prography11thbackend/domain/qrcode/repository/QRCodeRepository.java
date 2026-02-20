package com.prography11thbackend.domain.qrcode.repository;

import com.prography11thbackend.domain.qrcode.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    Optional<QRCode> findByHashValue(String hashValue);

    List<QRCode> findBySessionId(Long sessionId);

    Optional<QRCode> findBySessionIdAndIsActiveTrue(Long sessionId);
}
