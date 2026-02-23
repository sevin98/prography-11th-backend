package com.prography11thbackend.api.member.dto;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;

import java.time.Instant;
import java.time.ZoneId;

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
                member.getCreatedAt() != null ? member.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                member.getUpdatedAt() != null ? member.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null
        );
    }
}
