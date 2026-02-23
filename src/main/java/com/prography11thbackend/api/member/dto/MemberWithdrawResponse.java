package com.prography11thbackend.api.member.dto;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberStatus;

import java.time.Instant;
import java.time.ZoneId;

public record MemberWithdrawResponse(
        Long id,
        String loginId,
        String name,
        MemberStatus status,
        Instant updatedAt
) {
    public static MemberWithdrawResponse from(Member member) {
        return new MemberWithdrawResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getStatus(),
                member.getUpdatedAt() != null ? member.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
