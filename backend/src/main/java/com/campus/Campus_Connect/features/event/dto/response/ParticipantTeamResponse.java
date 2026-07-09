package com.campus.Campus_Connect.features.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantTeamResponse {

    private Integer teamId;

    private String teamName;

    private String leaderName;

    private Integer memberCount;

    private List<TeamMemberResponse> members;

}