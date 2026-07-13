package com.campus.Campus_Connect.features.event.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrantAccessRequest {

    @NotNull(message = "User id is required.")
    private Integer userId;

}