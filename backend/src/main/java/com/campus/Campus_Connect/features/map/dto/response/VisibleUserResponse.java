package com.campus.Campus_Connect.features.map.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisibleUserResponse {

    private Integer userId;

    private String username;

    private Double latitude;

    private Double longitude;

    private Boolean insideCampus;

    private String gender;
}