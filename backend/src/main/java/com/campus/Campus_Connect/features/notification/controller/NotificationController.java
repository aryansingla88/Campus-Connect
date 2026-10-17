package com.campus.Campus_Connect.features.notification.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.notification.dto.response.NotificationResponse;
import com.campus.Campus_Connect.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ============================================================
    // NOTIFY ME ON
    // ============================================================

    @PostMapping("/events/{eventId}/subscribe")
    public ApiResponse<Void> subscribeToEvent(
            @PathVariable Integer eventId) {

        return notificationService.subscribeToEvent(eventId);
    }

    // ============================================================
    // NOTIFY ME OFF
    // ============================================================

    @DeleteMapping("/events/{eventId}/subscribe")
    public ApiResponse<Void> unsubscribeFromEvent(
            @PathVariable Integer eventId) {

        return notificationService.unsubscribeFromEvent(eventId);
    }

    // ============================================================
    // CHECK NOTIFY ME STATUS
    // ============================================================

    @GetMapping("/events/{eventId}/subscribe")
    public ApiResponse<Boolean> isSubscribed(
            @PathVariable Integer eventId) {

        return notificationService.isSubscribed(eventId);
    }

    // ============================================================
// MY NOTIFICATIONS
// ============================================================

    @GetMapping
    public ApiResponse<java.util.List<NotificationResponse>>
    getMyNotifications() {

        return notificationService.getMyNotifications();
    }


// ============================================================
// UNREAD COUNT
// ============================================================

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {

        return notificationService.getUnreadCount();
    }


// ============================================================
// MARK ONE AS READ
// ============================================================

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Integer notificationId) {

        return notificationService.markAsRead(
                notificationId
        );
    }


// ============================================================
// MARK ALL AS READ
// ============================================================

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {

        return notificationService.markAllAsRead();
    }
}