package com.campus.Campus_Connect.features.auth.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.dto.request.RegisterRequest;
import com.campus.Campus_Connect.features.auth.dto.response.AuthResponse;
import com.campus.Campus_Connect.features.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

}