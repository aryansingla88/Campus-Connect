package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Integer> {

    boolean existsByEventIdAndUserId(Integer eventId, Integer userId);
}