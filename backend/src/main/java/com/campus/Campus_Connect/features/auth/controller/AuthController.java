package com.campus.Campus_Connect.features.auth.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.dto.request.*;
import com.campus.Campus_Connect.features.auth.dto.response.AuthResponse;
import com.campus.Campus_Connect.features.auth.dto.response.UserResponse;
import com.campus.Campus_Connect.features.auth.dto.response.VerifyPasswordResetOtpResponse;
import com.campus.Campus_Connect.features.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/password-reset/request")
    public ApiResponse<Void> requestPasswordReset(
            @RequestBody RequestPasswordResetRequest request
    ) {
        return authService.requestPasswordReset(request);
    }

    @PostMapping("/password-reset/verify")
    public ApiResponse<VerifyPasswordResetOtpResponse> verifyPasswordResetOtp(
            @RequestBody VerifyPasswordResetOtpRequest request
    ) {
        return authService.verifyPasswordResetOtp(request);
    }

    @PostMapping("/password-reset/reset")
    public ApiResponse<Void> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        return authService.resetPassword(request);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        System.out.println(SecurityContextHolder.getContext().getAuthentication());
        return authService.getCurrentUser();
    }
}