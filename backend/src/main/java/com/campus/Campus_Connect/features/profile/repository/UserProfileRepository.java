package com.campus.Campus_Connect.features.profile.repository;

import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository
        extends JpaRepository<UserProfile, Integer> {
}