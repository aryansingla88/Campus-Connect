package com.campus.Campus_Connect.features.event.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantsResponse {

    private List<ParticipantTeamResponse> teams;

    private List<SoloParticipantResponse> soloParticipants;

    private Integer totalParticipants;

}