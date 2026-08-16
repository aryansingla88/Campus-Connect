package com.campus.Campus_Connect.features.club.dto.response;

import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubDetailResponse {

    private Integer clubId;

    private String name;

    private String description;

    private String logoUrl;

    private Integer memberCount;

    private ClubMemberStatus memberStatus;

    private ClubMemberRole role;
}