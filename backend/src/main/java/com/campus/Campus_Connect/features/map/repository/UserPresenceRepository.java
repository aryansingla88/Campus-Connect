package com.campus.Campus_Connect.features.map.repository;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.map.entity.UserPresence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPresenceRepository
        extends JpaRepository<UserPresence, Integer> {

    Optional<UserPresence> findByUser(User user);

}