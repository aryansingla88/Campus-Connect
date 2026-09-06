package com.campus.Campus_Connect.features.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "otp_hash", nullable = false)
    private String otpHash;


    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;


    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;


    @Column(name = "otp_verified_at")
    private LocalDateTime otpVerifiedAt;


    @Column(name = "reset_token_hash")
    private String resetTokenHash;


    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;


    @Column(name = "used_at")
    private LocalDateTime usedAt;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}