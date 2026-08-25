package com.campus.Campus_Connect.features.auth.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.dto.request.LoginRequest;
import com.campus.Campus_Connect.features.auth.dto.request.RegisterRequest;
import com.campus.Campus_Connect.features.auth.dto.response.AuthResponse;
import com.campus.Campus_Connect.features.auth.dto.response.UserResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.profile.repository.UserProfileRepository;
import com.campus.Campus_Connect.features.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;
    private final CourseRepository courseRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    //Register
    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.failure("Username already exists.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.failure("Email already exists.");
        }

        courseRepository.findById(request.getCourseId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid or inactive course."));

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
                .admissionYear(request.getAdmissionYear())
                .rollNumber(request.getRollNumber())
                .gender(request.getGender())
                .dob(request.getDob())
                .build();

        userProfileRepository.save(profile);

        String token = jwtService.generateToken(user.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .build();

        return ApiResponse.success(
                response,
                "Registration successful."
        );

    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {

        User foundUser = userRepository.findByUsernameOrEmail(
                request.getIdentifier(),
                request.getIdentifier()
        ).orElseThrow(() ->
                new IllegalArgumentException("Username/Email not found.")
        );


        boolean isPasswordCorrect = passwordEncoder.matches(
                request.getPassword(),
                foundUser.getPasswordHash()
        );

        if (!isPasswordCorrect){
            return ApiResponse.failure("Incorrect password.");
        }

        if (Boolean.TRUE.equals(foundUser.getIsBanned())) {
            return ApiResponse.failure("Your account has been banned.");
        }

        String token = jwtService.generateToken(foundUser.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .build();


        return ApiResponse.success(
                response,
                "Login successful."
        );
    }

    public ApiResponse<UserResponse> getCurrentUser() {

        User currentUser = SecurityUtils.getCurrentUser();

        UserResponse response = UserResponse.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .role(currentUser.getRole())
                .build();

        return ApiResponse.success(
                response,
                "Current user fetched successfully."
        );
    }
}