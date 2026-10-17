package com.campus.Campus_Connect.features.notification.dto.response;

import com.campus.Campus_Connect.features.notification.entity.enums.NotificationType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Integer id;

    private NotificationType type;

    private String title;

    private String message;

    private Integer eventId;

    private Boolean read;

    private Instant createdAt;
}