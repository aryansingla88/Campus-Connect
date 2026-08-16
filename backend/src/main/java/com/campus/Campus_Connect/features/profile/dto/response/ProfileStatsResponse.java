package com.campus.Campus_Connect.features.profile.dto.response;

import lombok.Builder;

@Builder
public class ProfileStatsResponse {

    private Integer connectionCount;
    private Integer clubCount;
    private Integer interestCount;
    private Integer honorCount;
}