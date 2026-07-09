package com.campus.Campus_Connect.features.club.dto.response;

import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubResponse {

    private Integer clubId;

    private String name;

    private String logoUrl;

    private Integer memberCount;

    // Current authenticated user's membership status
    // null -> Not joined
    // PENDING -> Request sent
    // APPROVED -> Joined
    private ClubMemberStatus memberStatus;
}