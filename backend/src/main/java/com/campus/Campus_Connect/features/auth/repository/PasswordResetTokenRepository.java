package com.campus.Campus_Connect.features.auth.repository;

import com.campus.Campus_Connect.features.auth.entity.PasswordResetToken;
import com.campus.Campus_Connect.features.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken>
    findTopByUserOrderByCreatedAtDesc(User user);

    Optional<PasswordResetToken>
    findByResetTokenHash(String resetTokenHash);

    void deleteByUser(User user);
}