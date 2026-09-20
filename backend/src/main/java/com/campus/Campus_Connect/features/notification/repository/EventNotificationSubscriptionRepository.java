package com.campus.Campus_Connect.features.notification.repository;

import com.campus.Campus_Connect.features.notification.entity.EventNotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventNotificationSubscriptionRepository
        extends JpaRepository<EventNotificationSubscription, Integer> {

    boolean existsByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );

    Optional<EventNotificationSubscription> findByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );

    List<EventNotificationSubscription> findByEventId(
            Integer eventId
    );

    List<EventNotificationSubscription> findByUserId(
            Integer userId
    );

    void deleteByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );
}