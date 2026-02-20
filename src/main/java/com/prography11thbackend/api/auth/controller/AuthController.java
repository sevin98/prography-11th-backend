package com.prography11thbackend.api.auth.controller;

import com.prography11thbackend.api.auth.dto.LoginRequest;
import com.prography11thbackend.api.auth.dto.LoginResponse;
import com.prography11thbackend.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        boolean success = authService.login(request.loginId(), request.password());
        LoginResponse response = new LoginResponse(success);
        return ResponseEntity.ok(response);
    }
}
