package com.campus.Campus_Connect.features.profile.repository;

import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    @Override
    @EntityGraph(attributePaths = {"user", "hostelMetadata"})
    Optional<UserProfile> findById(Integer id);

    @Query("""
    SELECT p
    FROM UserProfile p
    JOIN FETCH p.user
    LEFT JOIN FETCH p.hostelMetadata
    WHERE p.userId = :userId
""")
    Optional<UserProfile> findByUserIdWithUser(Integer userId);
}
