package com.campus.Campus_Connect.features.profile.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileStatsResponse {

    private Integer connectionCount;
    private Integer clubCount;
    private Integer interestCount;
    private Integer honorCount;
}