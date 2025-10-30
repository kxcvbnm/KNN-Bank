package com.knn.knnbank.auth_users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knn.knnbank.auth_users.dtos.LoginRequest;
import com.knn.knnbank.auth_users.dtos.LoginResponse;
import com.knn.knnbank.auth_users.dtos.RegisterRequest;
import com.knn.knnbank.auth_users.dtos.ResetPasswordRequest;
import com.knn.knnbank.auth_users.service.AuthService;
import com.knn.knnbank.response.Response;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<String>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<?>> forgotPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        
        return ResponseEntity.ok(authService.forgotPassword(resetPasswordRequest.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<?>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        
        return ResponseEntity.ok(authService.updatePasswordViaResetCode(resetPasswordRequest));
    }

}
