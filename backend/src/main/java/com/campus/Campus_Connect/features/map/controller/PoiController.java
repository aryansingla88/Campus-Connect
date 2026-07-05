package com.campus.Campus_Connect.features.map.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.request.*;
import com.campus.Campus_Connect.features.map.dto.response.*;
import com.campus.Campus_Connect.features.map.service.PoiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/poi")
@RequiredArgsConstructor
public class PoiController {

    private final PoiService poiService;

    @PostMapping
    public ApiResponse<PoiResponse> createPoi(
            @Valid @RequestBody CreatePoiRequest request
    ) {
        return poiService.createPoi(request);
    }

    @GetMapping("/{id}")
    public ApiResponse<PoiResponse> getPoiById(
            @PathVariable Integer id
    ) {
        return poiService.getPoiById(id);
    }

    @PatchMapping("/{id}")
    public ApiResponse<PoiResponse> updatePoi(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePoiRequest request
    ) {
        return poiService.updatePoi(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePoi(
            @PathVariable Integer id
    ) {
        return poiService.deletePoi(id);
    }
    @GetMapping
    public ApiResponse<List<PoiResponse>> getAllPois() {
        return poiService.getAllPois();
    }

}