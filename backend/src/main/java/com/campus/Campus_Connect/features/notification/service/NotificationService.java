package com.campus.Campus_Connect.features.notification.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.notification.dto.response.NotificationResponse;
import com.campus.Campus_Connect.features.notification.entity.EventNotificationSubscription;
import com.campus.Campus_Connect.features.notification.entity.Notification;
import com.campus.Campus_Connect.features.notification.entity.enums.NotificationType;
import com.campus.Campus_Connect.features.notification.repository.EventNotificationSubscriptionRepository;
import com.campus.Campus_Connect.features.notification.repository.NotificationRepository;
import com.campus.Campus_Connect.features.registration.repository.EventRegistrationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EventRepository eventRepository;
    private final EventNotificationSubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    // ============================================================
    // ENABLE NOTIFY ME
    // ============================================================

    @Transactional
    public ApiResponse<Void> subscribeToEvent(Integer eventId) {

        User currentUser =
                SecurityUtils.getCurrentUser();

        Event event =
                eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure(
                    "Event not found."
            );
        }

        if (subscriptionRepository.existsByEventIdAndUserId(
                eventId,
                currentUser.getId()
        )) {
            return ApiResponse.failure(
                    "You are already subscribed to notifications."
            );
        }

        EventNotificationSubscription subscription =
                EventNotificationSubscription.builder()
                        .event(event)
                        .user(currentUser)
                        .build();

        subscriptionRepository.save(subscription);

        return ApiResponse.success(
                null,
                "Event notifications enabled."
        );
    }

    // ============================================================
    // DISABLE NOTIFY ME
    // ============================================================

    @Transactional
    public ApiResponse<Void> unsubscribeFromEvent(
            Integer eventId) {

        User currentUser =
                SecurityUtils.getCurrentUser();

        if (!eventRepository.existsById(eventId)) {
            return ApiResponse.failure(
                    "Event not found."
            );
        }

        if (!subscriptionRepository.existsByEventIdAndUserId(
                eventId,
                currentUser.getId()
        )) {
            return ApiResponse.failure(
                    "You are not subscribed to notifications."
            );
        }

        subscriptionRepository.deleteByEventIdAndUserId(
                eventId,
                currentUser.getId()
        );

        return ApiResponse.success(
                null,
                "Event notifications disabled."
        );
    }

    // ============================================================
    // CHECK NOTIFY ME STATUS
    // ============================================================

    public ApiResponse<Boolean> isSubscribed(
            Integer eventId) {

        User currentUser =
                SecurityUtils.getCurrentUser();

        if (!eventRepository.existsById(eventId)) {
            return ApiResponse.failure(
                    "Event not found."
            );
        }

        boolean subscribed =
                subscriptionRepository.existsByEventIdAndUserId(
                        eventId,
                        currentUser.getId()
                );

        return ApiResponse.success(
                subscribed,
                "Notification subscription status fetched."
        );
    }

    // ============================================================
// GET MY NOTIFICATIONS
// ============================================================

    public ApiResponse<java.util.List<NotificationResponse>>
    getMyNotifications() {

        User currentUser =
                SecurityUtils.getCurrentUser();

        var notifications =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                currentUser.getId()
                        );

        var response =
                notifications.stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse.success(
                response,
                "Notifications fetched successfully."
        );
    }


// ============================================================
// GET UNREAD COUNT
// ============================================================

    public ApiResponse<Long> getUnreadCount() {

        User currentUser =
                SecurityUtils.getCurrentUser();

        long count =
                notificationRepository
                        .countByUserIdAndReadFalse(
                                currentUser.getId()
                        );

        return ApiResponse.success(
                count,
                "Unread notification count fetched."
        );
    }


// ============================================================
// MARK ONE AS READ
// ============================================================

    @Transactional
    public ApiResponse<Void> markAsRead(
            Integer notificationId) {

        User currentUser =
                SecurityUtils.getCurrentUser();

        var notification =
                notificationRepository
                        .findById(notificationId)
                        .orElse(null);

        if (notification == null) {
            return ApiResponse.failure(
                    "Notification not found."
            );
        }

        if (!notification.getUser().getId()
                .equals(currentUser.getId())) {

            return ApiResponse.failure(
                    "Notification does not belong to you."
            );
        }

        notification.setRead(true);

        notificationRepository.save(notification);

        return ApiResponse.success(
                null,
                "Notification marked as read."
        );
    }


// ============================================================
// MARK ALL AS READ
// ============================================================

    @Transactional
    public ApiResponse<Void> markAllAsRead() {

        User currentUser =
                SecurityUtils.getCurrentUser();

        var notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                currentUser.getId()
                        );

        notifications.forEach(
                notification -> notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);

        return ApiResponse.success(
                null,
                "All notifications marked as read."
        );
    }


// ============================================================
// MAPPER
// ============================================================

    private NotificationResponse toResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .eventId(
                        notification.getEvent() != null
                                ? notification.getEvent().getId()
                                : null
                )
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }


    // ============================================================
// CREATE NOTIFICATION FOR ONE USER
// ============================================================

    @Transactional
    public void createNotification(
            User user,
            NotificationType type,
            String title,
            String message,
            Event event
    ) {

        Notification notification =
                Notification.builder()
                        .user(user)
                        .type(type)
                        .title(title)
                        .message(message)
                        .event(event)
                        .read(false)
                        .build();

        notificationRepository.save(notification);
    }

    // ============================================================
// CREATE NOTIFICATION FOR MULTIPLE USERS
// ============================================================

    @Transactional
    public void createNotifications(
            java.util.List<User> users,
            NotificationType type,
            String title,
            String message,
            Event event
    ) {

        java.util.List<Notification> notifications =
                users.stream()
                        .map(user ->
                                Notification.builder()
                                        .user(user)
                                        .type(type)
                                        .title(title)
                                        .message(message)
                                        .event(event)
                                        .read(false)
                                        .build()
                        )
                        .toList();

        notificationRepository.saveAll(notifications);
    }

// ============================================================
// GET REGISTERED USERS
// ============================================================

    public java.util.List<User> getRegisteredUsers(
            Integer eventId
    ) {

        return registrationRepository
                .findByEventId(eventId)
                .stream()
                .map(registration -> registration.getUser())
                .distinct()
                .toList();
    }


// ============================================================
// GET NOTIFY-ME USERS
// ============================================================

    public java.util.List<User> getNotificationSubscribers(
            Integer eventId
    ) {

        return subscriptionRepository
                .findByEventId(eventId)
                .stream()
                .map(subscription -> subscription.getUser())
                .distinct()
                .toList();
    }

    // ============================================================
// EVENT CREATED
// ============================================================

    @Transactional
    public void notifyEventCreated(Event event) {

        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return;
        }

        createNotifications(
                users,
                NotificationType.EVENT_CREATED,
                "New Event",
                event.getTitle() + " has been created.",
                event
        );
    }

}