package com.prography11thbackend.api.member.dto;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;

public record MemberResponse(
        Long id,
        String loginId,
        String name,
        MemberRole role,
        MemberStatus status
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getRole(),
                member.getStatus()
        );
    }
}
