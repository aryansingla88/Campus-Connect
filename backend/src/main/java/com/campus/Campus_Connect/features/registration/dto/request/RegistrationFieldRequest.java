package com.campus.Campus_Connect.features.registration.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFieldRequest {

    private Integer id;

    private String fieldLabel;

    private String fieldType;

    private Boolean required;

    private String placeholder;

    private Integer fieldOrder;

    private Boolean individual;

    private List<Option> options;

    //    ---------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Option {

        private String optionValue;

        private Integer optionOrder;
    }
}
