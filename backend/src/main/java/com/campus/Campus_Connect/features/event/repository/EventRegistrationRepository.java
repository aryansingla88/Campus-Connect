package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository
        extends JpaRepository<EventRegistration, Integer> {

    boolean existsByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );

    List<EventRegistration> findByEventId(
            Integer eventId
    );

    Optional<EventRegistration> findByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );
}