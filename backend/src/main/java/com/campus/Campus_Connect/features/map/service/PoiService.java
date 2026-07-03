package com.campus.Campus_Connect.features.map.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.request.CreatePoiRequest;
import com.campus.Campus_Connect.features.map.dto.response.PoiResponse;
import com.campus.Campus_Connect.features.map.entity.Poi;
import com.campus.Campus_Connect.features.map.repository.PoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PoiService {

    private final PoiRepository poiRepository;

    public ApiResponse<PoiResponse> createPoi(CreatePoiRequest request) {

        Poi poi = Poi.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Poi savedPoi = poiRepository.save(poi);

        PoiResponse response = PoiResponse.builder()
                .id(String.valueOf(savedPoi.getId()))
                .name(savedPoi.getName())
                .category(savedPoi.getCategory().name())
                .description(savedPoi.getDescription())
                .latitude(savedPoi.getLatitude())
                .longitude(savedPoi.getLongitude())
                .build();

        return ApiResponse.success(
                response,
                "POI created successfully."
        );
    }
}