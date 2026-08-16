package com.campus.Campus_Connect.features.registration.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFieldRequest {

    private String fieldLabel;

    private String fieldType;

    private Boolean required;

    private String placeholder;

    private Integer fieldOrder;

    private List<RegistrationFieldOptionRequest> options;
}