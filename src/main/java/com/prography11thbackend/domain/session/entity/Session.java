package com.prography11thbackend.domain.session.entity;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Builder
    public Session(String title, String description, LocalDateTime startTime, Cohort cohort, SessionStatus status) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.cohort = cohort;
        this.status = status;
    }

    public void update(String title, String description, LocalDateTime startTime) {
        if (this.status == SessionStatus.CANCELLED) {
            throw new IllegalStateException("취소된 일정은 수정할 수 없습니다.");
        }
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (startTime != null) {
            this.startTime = startTime;
        }
    }

    public void cancel() {
        this.status = SessionStatus.CANCELLED;
    }

    public void start() {
        this.status = SessionStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = SessionStatus.COMPLETED;
    }
}
