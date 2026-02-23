package com.prography11thbackend.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MemberRegisterRequest(
        @NotBlank(message = "로그인 ID는 필수입니다")
        String loginId,
        @NotBlank(message = "비밀번호는 필수입니다")
        String password,
        @NotBlank(message = "이름은 필수입니다")
        String name,
        String phone,
        @NotNull(message = "기수 ID는 필수입니다")
        @Positive(message = "기수 ID는 양수여야 합니다")
        Long cohortId,
        @NotNull(message = "파트 ID는 필수입니다")
        @Positive(message = "파트 ID는 양수여야 합니다")
        Long partId,
        Long teamId
) {
}
