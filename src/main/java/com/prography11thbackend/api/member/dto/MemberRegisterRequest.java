package com.prography11thbackend.api.member.dto;

public record MemberRegisterRequest(
        String loginId,
        String password,
        String name,
        Long cohortId,
        String part,
        Long teamId
) {
}
