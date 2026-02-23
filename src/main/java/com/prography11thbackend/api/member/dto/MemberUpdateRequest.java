package com.prography11thbackend.api.member.dto;

public record MemberUpdateRequest(
        String name,
        String phone,
        Long cohortId,
        Long partId,
        Long teamId
) {
}
