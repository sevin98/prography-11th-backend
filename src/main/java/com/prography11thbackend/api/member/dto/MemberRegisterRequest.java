package com.prography11thbackend.api.member.dto;

public record MemberRegisterRequest(
        String loginId,
        String password,
        String name,
        String phone,
        Long cohortId,
        Long partId,
        Long teamId
) {
}
