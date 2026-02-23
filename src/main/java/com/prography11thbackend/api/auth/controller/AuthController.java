package com.prography11thbackend.api.auth.controller;

import com.prography11thbackend.api.auth.dto.LoginRequest;
import com.prography11thbackend.api.auth.dto.LoginResponse;
import com.prography11thbackend.domain.auth.service.AuthService;
import com.prography11thbackend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = LoginResponse.from(authService.login(request.loginId(), request.password()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
