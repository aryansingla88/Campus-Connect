package com.campus.Campus_Connect.features.map.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.request.CreatePoiRequest;
import com.campus.Campus_Connect.features.map.dto.response.PoiResponse;
import com.campus.Campus_Connect.features.map.service.PoiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}