package com.prography11thbackend.api.member.dto;

import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

public record MemberResponse(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        Integer generation,
        String partName,
        String teamName,
        Integer deposit,
        Instant createdAt,
        Instant updatedAt
) {
    public static MemberResponse from(Member member) {
        return from(member, null);
    }

    public static MemberResponse from(Member member, CohortMember cohortMember) {
        return from(member, cohortMember, null);
    }

    public static MemberResponse from(Member member, CohortMember cohortMember, Integer deposit) {
        Integer generation = null;
        String partName = null;
        String teamName = null;

        if (cohortMember != null) {
            generation = cohortMember.getCohort().getNumber();
            partName = cohortMember.getPart() != null ? cohortMember.getPart().name() : null;
            teamName = cohortMember.getTeam() != null ? cohortMember.getTeam().getName() : null;
        }

        return new MemberResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getStatus(),
                member.getRole(),
                generation,
                partName,
                teamName,
                deposit,
                member.getCreatedAt() != null ? member.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                member.getUpdatedAt() != null ? member.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
