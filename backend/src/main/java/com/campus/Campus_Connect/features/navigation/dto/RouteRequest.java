package com.campus.Campus_Connect.features.navigation.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteRequest {
    private Double userLat;
    private Double userLng;
    private String destinationType;
    private Long destinationId;
}