package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFieldResponse {

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

        private Integer id;

        private String optionValue;

        private Integer optionOrder;
    }
}
