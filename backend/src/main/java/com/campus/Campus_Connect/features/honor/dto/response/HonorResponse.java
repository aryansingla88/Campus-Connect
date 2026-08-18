package com.campus.Campus_Connect.features.honor.dto.response;

import com.campus.Campus_Connect.features.honor.enums.HonorType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class HonorResponse {

    private Integer honorId;

    private HonorType type;

    private String title;

    private String subtitle;

    private String iconUrl;

    private Integer eventId;

    private Integer priority;

    private Instant awardedAt;
}