package com.campus.Campus_Connect.features.settings.dto;

import com.campus.Campus_Connect.features.settings.enums.ShowPresence;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceResponse {

    private Boolean showPhone;

    private Boolean showSocials;

    private ShowPresence showPresence;

    private Boolean notifyConnections;

    private Boolean notifyEvents;

    private Boolean notifyPosts;

    private String theme;
}