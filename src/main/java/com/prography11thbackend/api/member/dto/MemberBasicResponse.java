package com.prography11thbackend.api.member.dto;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;

import java.time.Instant;
import java.time.ZoneOffset;

public record MemberBasicResponse(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        Instant createdAt,
        Instant updatedAt
) {
    public static MemberBasicResponse from(Member member) {
        return new MemberBasicResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getStatus(),
                member.getRole(),
                member.getCreatedAt() != null ? member.getCreatedAt().toInstant(ZoneOffset.UTC) : null,
                member.getUpdatedAt() != null ? member.getUpdatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
