package com.campus.Campus_Connect.features.event.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDisplayResponse {

    private String name;

    private String subtitle;

    private String avatarUrl;

}