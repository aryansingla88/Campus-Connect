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


import com.campus.Campus_Connect.features.auth.dto.request.RequestPasswordResetRequest;
import com.campus.Campus_Connect.features.auth.dto.request.VerifyPasswordResetOtpRequest;
import com.campus.Campus_Connect.features.auth.dto.request.ResetPasswordRequest;
import com.campus.Campus_Connect.features.auth.dto.response.VerifyPasswordResetOtpResponse;
import com.campus.Campus_Connect.features.auth.entity.PasswordResetToken;
import com.campus.Campus_Connect.features.auth.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final CourseRepository courseRepository;

    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

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

    private String generateOtp() {

        SecureRandom secureRandom = new SecureRandom();

        int otp = 100000 + secureRandom.nextInt(900000);

        return String.valueOf(otp);
    }

    private String generateResetToken() {

        SecureRandom secureRandom = new SecureRandom();

        byte[] tokenBytes = new byte[32];

        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private String hashResetToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {

                String hex =
                        Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to hash reset token.",
                    e
            );
        }
    }

    @Transactional
    public ApiResponse<Void> requestPasswordReset(
            RequestPasswordResetRequest request
    ) {

        User user = userRepository
                .findByUsernameOrEmail(
                        request.getEmail(),
                        request.getEmail()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found.")
                );
        //correct work around for now


        /*
         * Check 60-second cooldown.
         */
        Optional<PasswordResetToken> latestToken =
                passwordResetTokenRepository
                        .findTopByUserOrderByCreatedAtDesc(user);


        if (latestToken.isPresent()) {

            LocalDateTime lastRequestTime =
                    latestToken.get().getCreatedAt();

            if (
                    lastRequestTime
                            .plusSeconds(60)
                            .isAfter(LocalDateTime.now())
            ) {

                return ApiResponse.failure(
                        "Please wait before requesting another OTP."
                );
            }
        }


        /*
         * Old OTP must become invalid when a new one is generated.
         */
        passwordResetTokenRepository.deleteByUser(user);


        /*
         * Generate and hash OTP.
         */
        String otp = generateOtp();

        String otpHash =
                passwordEncoder.encode(otp);


        /*
         * Create new reset record.
         */
        PasswordResetToken passwordResetToken =
                PasswordResetToken.builder()

                        .user(user)

                        .otpHash(otpHash)

                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(10)
                        )

                        .attemptCount(0)

                        .createdAt(LocalDateTime.now())

                        .build();


        passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp
        );


        return ApiResponse.success(
                null,
                "Password reset OTP generated successfully."
        );
    }

    @Transactional
    public ApiResponse<VerifyPasswordResetOtpResponse> verifyPasswordResetOtp(
            VerifyPasswordResetOtpRequest request
    ) {

        /*
         * Find the user using the submitted email.
         */
        User user = userRepository
                .findByUsernameOrEmail(
                        request.getEmail(),
                        request.getEmail()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found.")
                );


        /*
         * Find the latest password reset request for this user.
         */
        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository
                        .findTopByUserOrderByCreatedAtDesc(user)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No password reset request found."
                                )
                        );


        /*
         * Check whether this reset request has already been used.
         */
        if (passwordResetToken.getUsedAt() != null) {

            return ApiResponse.failure(
                    "This password reset request has already been used."
            );
        }


        /*
         * Check whether OTP has expired.
         */
        if (passwordResetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            return ApiResponse.failure(
                    "OTP has expired. Please request a new OTP."
            );
        }


        /*
         * Prevent verification if OTP was already successfully verified.
         *
         * A successful OTP verification should produce one reset token.
         * The user should not be able to repeatedly verify the same OTP
         * and generate multiple reset tokens.
         */
        if (passwordResetToken.getOtpVerifiedAt() != null) {

            return ApiResponse.failure(
                    "OTP has already been verified."
            );
        }


        /*
         * Maximum 5 OTP attempts.
         */
        if (passwordResetToken.getAttemptCount() >= 5) {

            return ApiResponse.failure(
                    "Maximum OTP verification attempts exceeded. Please request a new OTP."
            );
        }


        /*
         * Verify submitted OTP against BCrypt hash.
         */
        boolean isOtpCorrect =
                passwordEncoder.matches(
                        request.getOtp(),
                        passwordResetToken.getOtpHash()
                );


        /*
         * Wrong OTP.
         */
        if (!isOtpCorrect) {

            passwordResetToken.setAttemptCount(
                    passwordResetToken.getAttemptCount() + 1
            );

            passwordResetTokenRepository.save(passwordResetToken);

            int remainingAttempts =
                    5 - passwordResetToken.getAttemptCount();

            return ApiResponse.failure(
                    "Invalid OTP. Remaining attempts: "
                            + remainingAttempts
            );
        }


        /*
         * OTP is correct.
         * Generate a cryptographically secure opaque reset token.
         */
        String rawResetToken = generateResetToken();


        /*
         * Store only the SHA-256 hash.
         * The raw token is returned to the client once and is never stored.
         */
        String resetTokenHash =
                hashResetToken(rawResetToken);


        /*
         * Mark OTP as verified and activate reset token.
         */
        passwordResetToken.setOtpVerifiedAt(
                LocalDateTime.now()
        );

        passwordResetToken.setResetTokenHash(
                resetTokenHash
        );

        passwordResetToken.setResetTokenExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(10)
        );

        passwordResetTokenRepository.save(passwordResetToken);


        /*
         * Return the raw reset token once.
         */
        VerifyPasswordResetOtpResponse response =
                new VerifyPasswordResetOtpResponse(
                        rawResetToken
                );


        return ApiResponse.success(
                response,
                "OTP verified successfully."
        );
    }


    @Transactional
    public ApiResponse<Void> resetPassword(
            ResetPasswordRequest request
    ) {

        /*
         * Hash the raw reset token received from the client.
         *
         * We never store the raw reset token in the database.
         */
        String resetTokenHash =
                hashResetToken(request.getResetToken());


        /*
         * Find the reset token record using its hash.
         */
        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository
                        .findByResetTokenHash(resetTokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid reset token."
                                )
                        );


        /*
         * Prevent reuse of an already consumed token.
         */
        if (passwordResetToken.getUsedAt() != null) {

            return ApiResponse.failure(
                    "This reset token has already been used."
            );
        }


        /*
         * Check whether the reset token has expired.
         */
        if (
                passwordResetToken.getResetTokenExpiresAt() == null
                        ||
                        passwordResetToken
                                .getResetTokenExpiresAt()
                                .isBefore(LocalDateTime.now())
        ) {

            return ApiResponse.failure(
                    "Reset token has expired."
            );
        }


        /*
         * Get the user associated with this reset request.
         */
        User user = passwordResetToken.getUser();


        /*
         * Hash the new password using BCrypt.
         */
        String newPasswordHash =
                passwordEncoder.encode(
                        request.getNewPassword()
                );


        /*
         * Update the user's password.
         */
        user.setPasswordHash(newPasswordHash);

        userRepository.save(user);


        /*
         * Consume the reset token permanently.
         */
        passwordResetToken.setUsedAt(
                LocalDateTime.now()
        );

        passwordResetTokenRepository.save(
                passwordResetToken
        );


        return ApiResponse.success(
                null,
                "Password reset successfully."
        );
    }
}
