package com.campus.Campus_Connect.features.map.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.request.*;
import com.campus.Campus_Connect.features.map.dto.response.*;
import com.campus.Campus_Connect.features.map.entity.Poi;
import com.campus.Campus_Connect.features.map.repository.PoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PoiService {

    private final PoiRepository poiRepository;

    // Create POI
    public ApiResponse<PoiResponse> createPoi(CreatePoiRequest request) {

        Poi poi = Poi.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Poi savedPoi = poiRepository.save(poi);

        return ApiResponse.success(
                mapToResponse(savedPoi),
                "POI created successfully."
        );
    }

    // Get POI by ID
    public ApiResponse<PoiResponse> getPoiById(Integer id) {

        Optional<Poi> poi = poiRepository.findById(id);

        if (poi.isEmpty()) {
            return ApiResponse.failure("POI not found.");
        }

        Poi foundPoi = poi.get();

        return ApiResponse.success(
                mapToResponse(foundPoi),
                "POI fetched successfully."
        );
    }

    public ApiResponse<PoiResponse> updatePoi(
            Integer id,
            UpdatePoiRequest request
    ) {

        Optional<Poi> poi = poiRepository.findById(id);

        if (poi.isEmpty()) {
            return ApiResponse.failure("POI not found.");
        }

        Poi existingPoi = poi.get();

        if (request.getName() != null) {
            existingPoi.setName(request.getName());
        }

        if (request.getCategory() != null) {
            existingPoi.setCategory(request.getCategory());
        }

        if (request.getDescription() != null) {
            existingPoi.setDescription(request.getDescription());
        }

        if (request.getLatitude() != null) {
            existingPoi.setLatitude(request.getLatitude());
        }

        if (request.getLongitude() != null) {
            existingPoi.setLongitude(request.getLongitude());
        }

        Poi updatedPoi = poiRepository.save(existingPoi);

        return ApiResponse.success(
                mapToResponse(updatedPoi),
                "POI updated successfully."
        );
    }

    public ApiResponse<Void> deletePoi(Integer id) {

        Optional<Poi> poi = poiRepository.findById(id);

        if (poi.isEmpty()) {
            return ApiResponse.failure("POI not found.");
        }

        Poi existingPoi = poi.get();

        poiRepository.delete(existingPoi);

        return ApiResponse.success(
                null,
                "POI deleted successfully."
        );
    }

    public ApiResponse<List<PoiResponse>> getAllPois() {

        List<Poi> pois = poiRepository.findAll();

        List<PoiResponse> response = new ArrayList<>();

        for (Poi poi : pois) {
            response.add(mapToResponse(poi));
        }

        return ApiResponse.success(
                response,
                "POIs fetched successfully."
        );
    }

    // Entity -> Response DTO
    private PoiResponse mapToResponse(Poi poi) {

        return PoiResponse.builder()
                .id(String.valueOf(poi.getId()))
                .name(poi.getName())
                .category(poi.getCategory().name())
                .description(poi.getDescription())
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .iconType(poi.getIconType())
                .visibility(poi.getVisibility())
                .build();
    }
}