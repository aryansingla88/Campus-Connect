package com.campus.Campus_Connect.features.auth.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.dto.request.LoginRequest;
import com.campus.Campus_Connect.features.auth.dto.request.RegisterRequest;
import com.campus.Campus_Connect.features.auth.dto.response.AuthResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    //Register
    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.failure("Username already exists.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.failure("Email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .build();

        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .courseId(request.getCourseId())
                .branch(request.getBranch())
                .admissionYear(request.getAdmissionYear())
                .rollNumber(request.getRollNumber())
                .gender(request.getGender())
                .dob(request.getDob())
                .build();

        userProfileRepository.save(profile);

        AuthResponse response = AuthResponse.builder()
                .build();

        return ApiResponse.success(
                response,
                "Registration successful."
        );

    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {

        Optional<User> user = userRepository.findByUsernameOrEmail(
                request.getIdentifier(),
                request.getIdentifier()
        );

        if (user.isEmpty()) {
            return ApiResponse.failure("Username/Email not found.");
        }

        User foundUser = user.get();

        boolean isPasswordCorrect = passwordEncoder.matches(
                request.getPassword(),
                foundUser.getPasswordHash()
        );

        if (!isPasswordCorrect){
            return ApiResponse.failure("Incorrect password.");
        }

        AuthResponse response = AuthResponse.builder()
                .build();

        return ApiResponse.success(
                response,
                "Login successful."
        );
    }
}