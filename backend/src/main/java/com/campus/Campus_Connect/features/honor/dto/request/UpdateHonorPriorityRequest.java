package com.campus.Campus_Connect.features.honor.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateHonorPriorityRequest {

    @NotNull
    private Integer honorId;

    @NotNull
    @Positive
    private Integer priority;
}