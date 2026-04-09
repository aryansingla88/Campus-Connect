package com.campus.Campus_Connect.controller;

import com.campus.Campus_Connect.model.User;
import com.campus.Campus_Connect.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        try {
            authService.registerUser(user);
            return Map.of("message", "User registered successfully");
        } catch (RuntimeException e) {
            return Map.of("message", e.getMessage());
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {

        String identifier = (user.getEmail() != null && !user.getEmail().isEmpty())
                ? user.getEmail()
                : user.getUsername();

        User loggedInUser = authService.loginUser(identifier, user.getPassword());

        if (loggedInUser != null) {
            return Map.of(
                    "success", true,
                    "message", "Login successful",
                    "user_id", loggedInUser.getId()
            );
        }

        return Map.of(
                "success", false,
                "message", "Invalid email or password",
                "user_id", 0
        );
    }
}