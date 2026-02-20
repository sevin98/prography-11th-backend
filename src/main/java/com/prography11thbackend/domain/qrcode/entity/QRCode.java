package com.prography11thbackend.domain.qrcode.entity;

import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QRCode extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String hashValue; // UUID 기반

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 생성 시점 + 24시간

    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public QRCode(Session session) {
        this.hashValue = UUID.randomUUID().toString();
        this.session = session;
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.isActive = true;
    }

    public void expire() {
        this.isActive = false;
    }

    public boolean isExpired() {
        return !isActive || LocalDateTime.now().isAfter(expiresAt);
    }
}
