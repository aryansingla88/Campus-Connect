package com.campus.Campus_Connect.features.club.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubResponse {

    private Integer clubId;

    private String name;

    private String clubType;

    private String logoUrl;

    private Integer memberCount;

    // Response-only membership classification; actual member roles remain internal.
    private ClubResponseMemberStatus memberStatus;
}
