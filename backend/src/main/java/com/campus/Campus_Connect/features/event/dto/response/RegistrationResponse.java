package com.campus.Campus_Connect.features.event.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {

    private Integer registrationId;

    private Boolean registered;

    private String status;
}