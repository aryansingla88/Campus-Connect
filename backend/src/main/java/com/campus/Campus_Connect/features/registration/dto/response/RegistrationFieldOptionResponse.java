package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFieldOptionResponse {

    private Integer id;

    private String optionValue;

    private Integer optionOrder;
}