package com.campus.Campus_Connect.features.registration.repository;

import com.campus.Campus_Connect.features.registration.entity.RegistrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationConfigRepository extends JpaRepository<RegistrationConfig, Integer> {
    Optional<RegistrationConfig> findByEventId(Integer eventId);
}
