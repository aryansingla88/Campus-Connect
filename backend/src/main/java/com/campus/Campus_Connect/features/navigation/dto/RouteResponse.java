package com.campus.Campus_Connect.features.navigation.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteResponse {
    private Double totalDistanceMeters;
    private Integer estimatedTimeMinutes;
    private List<LatLngDto> path;
}