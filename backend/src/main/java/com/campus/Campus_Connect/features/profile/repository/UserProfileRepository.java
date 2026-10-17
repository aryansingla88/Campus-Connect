package com.campus.Campus_Connect.features.profile.repository;

import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    @Query("""
    SELECT p
    FROM UserProfile p
    JOIN FETCH p.user
    WHERE p.userId = :userId
""")
    Optional<UserProfile> findByUserIdWithUser(Integer userId);
}