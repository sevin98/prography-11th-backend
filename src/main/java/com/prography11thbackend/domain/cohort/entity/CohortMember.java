package com.prography11thbackend.domain.cohort.entity;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cohort_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CohortMember extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private Integer excuseCount = 0; // 공결 횟수

    @Builder
    public CohortMember(Member member, Cohort cohort, Part part, Team team) {
        this.member = member;
        this.cohort = cohort;
        this.part = part;
        this.team = team;
        this.excuseCount = 0;
    }

    public void increaseExcuseCount() {
        this.excuseCount++;
    }

    public void decreaseExcuseCount() {
        if (this.excuseCount > 0) {
            this.excuseCount--;
        }
    }

    public void updatePart(Part part) {
        this.part = part;
    }

    public void updateTeam(Team team) {
        this.team = team;
    }
}
