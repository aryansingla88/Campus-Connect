package com.campus.Campus_Connect.features.notification.repository;

import com.campus.Campus_Connect.features.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Integer userId
    );

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Integer userId
    );

    long countByUserIdAndReadFalse(
            Integer userId
    );

    List<Notification> findByUserIdAndId(
            Integer userId,
            Integer notificationId
    );
}