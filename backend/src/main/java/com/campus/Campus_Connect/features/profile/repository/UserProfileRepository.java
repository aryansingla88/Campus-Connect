package com.campus.Campus_Connect.features.profile.repository;

import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    // Add this line so Spring Data JPA generates the finder query
    Optional<UserProfile> findByUserId(Integer userId);
}