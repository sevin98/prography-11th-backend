package com.prography11thbackend.api.member.dto;

import jakarta.validation.constraints.Positive;

public record MemberUpdateRequest(
        String name,
        String phone,
        Long cohortId,
        @Positive(message = "파트 ID는 양수여야 합니다")
        Long partId,
        Long teamId
) {
}
