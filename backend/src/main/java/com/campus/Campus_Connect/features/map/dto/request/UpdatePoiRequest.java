package com.campus.Campus_Connect.features.map.dto.request;

import com.campus.Campus_Connect.features.map.enums.PoiCategory;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePoiRequest {

    @Size(max = 100, message = "POI name cannot exceed 100 characters.")
    private String name;

    private PoiCategory category;

    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;

    private Double latitude;

    private Double longitude;
}