package com.campus.Campus_Connect.features.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoloParticipantResponse {

    private Integer registrationId;

    private Integer userId;

    private String name;

    private String subtitle;

    private String avatarUrl;

}