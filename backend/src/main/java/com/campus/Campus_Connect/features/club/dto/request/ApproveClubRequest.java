package com.campus.Campus_Connect.features.club.dto.request;

import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveClubRequest {
    private ClubMemberRole role;
}
