package com.campus.Campus_Connect.features.event.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrantAccessRequest {

    @NotNull(message = "User id is required.")
    @JsonProperty("user_id")
    private Integer userId;

}