package com.campus.Campus_Connect.features.club.dto.response;

import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMembershipResponse {

    private Integer clubId;

    private ClubMemberStatus memberStatus;
}