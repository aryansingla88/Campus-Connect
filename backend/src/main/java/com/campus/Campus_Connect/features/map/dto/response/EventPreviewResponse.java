package com.campus.Campus_Connect.features.map.dto.response;

import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventPreviewResponse {

    private Integer id;

    private String title;

    private String description;

    private String posterUrl;

    private Instant startTime;

    private Instant endTime;

    private String venue;

    private RegistrationType registrationType;

    private String registrationLink;

    private Boolean isJoined;

    private Boolean isReminderEnabled;

    private Integer priority;

    private List<HostPreview> hosts;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HostPreview {

        private Integer userId;

        private String fullName;

        private String avatarUrl;

        private String role;
    }
}