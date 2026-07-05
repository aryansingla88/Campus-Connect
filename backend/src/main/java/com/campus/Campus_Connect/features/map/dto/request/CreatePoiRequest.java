package com.campus.Campus_Connect.features.map.dto.request;

import com.campus.Campus_Connect.features.map.enums.PoiCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePoiRequest {

    @NotBlank(message = "POI name is required.")
    @Size(max = 100, message = "POI name cannot exceed 100 characters.")
    private String name;

    @NotNull(message = "Category is required.")
    private PoiCategory category;

    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;

    @NotNull(message = "Latitude is required.")
    private Double latitude;

    @NotNull(message = "Longitude is required.")
    private Double longitude;
}