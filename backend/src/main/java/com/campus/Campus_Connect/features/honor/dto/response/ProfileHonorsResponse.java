package com.campus.Campus_Connect.features.honor.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProfileHonorsResponse {

    private Integer honorRank;

    private List<HonorResponse> badges;

    private List<HonorResponse> medals;
}