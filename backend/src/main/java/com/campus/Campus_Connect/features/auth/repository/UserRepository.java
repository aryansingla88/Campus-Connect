package com.campus.Campus_Connect.features.auth.repository;

import com.campus.Campus_Connect.features.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsernameOrEmail(
            String username,
            String email
    );

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("""
    SELECT u
    FROM User u
    WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(u.profile.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
    ORDER BY u.profile.fullName
""")
    List<User> searchUsers(
            @Param("query") String query
    );
}