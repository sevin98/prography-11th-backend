package com.prography11thbackend.domain.attendance.entity;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private Integer penalty; // 패널티 금액

    @Column
    private LocalDateTime checkedAt; // 출석 체크 시간

    @Builder
    public Attendance(Member member, Session session, AttendanceStatus status, Integer penalty, LocalDateTime checkedAt) {
        this.member = member;
        this.session = session;
        this.status = status;
        this.penalty = penalty;
        this.checkedAt = checkedAt;
    }

    public void updateStatus(AttendanceStatus newStatus, Integer newPenalty) {
        this.status = newStatus;
        this.penalty = newPenalty;
    }

    public Integer getPenaltyDifference(Integer oldPenalty) {
        return this.penalty - oldPenalty;
    }
}
