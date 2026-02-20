package com.prography11thbackend.api.auth.dto;

public record LoginRequest(
        String loginId,
        String password
) {
}
