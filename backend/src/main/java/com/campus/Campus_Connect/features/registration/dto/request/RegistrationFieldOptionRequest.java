package com.campus.Campus_Connect.features.registration.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFieldOptionRequest {

    private String optionValue;

    private Integer optionOrder;
}