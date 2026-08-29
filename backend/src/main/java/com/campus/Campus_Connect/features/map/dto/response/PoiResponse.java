package com.campus.Campus_Connect.features.map.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoiResponse {

    private Integer id; // Mapped to DB Entity Integer ID directly

    private String name;

    private String category;

    private String description;

    private Double latitude;

    private Double longitude;

    private String iconType;

    private String visibility;
}